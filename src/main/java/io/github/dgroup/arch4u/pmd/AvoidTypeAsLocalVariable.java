package io.github.dgroup.arch4u.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassType;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTInitializer;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;

public class AvoidTypeAsLocalVariable extends AbstractJavaRule {

    private static final PropertyDescriptor<List<String>> OBJECT_MAPPER_DESCRIPTOR =
        PropertyFactory.stringListProperty("objectMapperClasses")
            .desc("Full names of the ObjectMapper classes")
            .defaultValues("com.fasterxml.jackson.databind.ObjectMapper")
            .build();

    private static final PropertyDescriptor<List<String>> ANNOTATIONS_DESCRIPTOR =
        PropertyFactory.stringListProperty("annotations")
            .desc("Full name of the method annotations in which it's allowed to use ObjectMapper as a field")
            .defaultValues(
                "javax.annotation.PostConstruct",
                "org.springframework.context.annotation.Bean"
            )
            .build();

    private static final PropertyDescriptor<Boolean> CHECK_SUBTYPES_DESCRIPTOR =
        PropertyFactory.booleanProperty("checkSubtypes")
            .desc("The property matches whether the ObjectMapper subtypes should be checked")
            .defaultValue(true)
            .build();

    private List<String> objectMapperClasses;
    private List<String> annotations;

    public AvoidTypeAsLocalVariable() {
        definePropertyDescriptor(OBJECT_MAPPER_DESCRIPTOR);
        definePropertyDescriptor(ANNOTATIONS_DESCRIPTOR);
        definePropertyDescriptor(CHECK_SUBTYPES_DESCRIPTOR);

        objectMapperClasses = getProperty(OBJECT_MAPPER_DESCRIPTOR);
        annotations = getProperty(ANNOTATIONS_DESCRIPTOR);
    }

    @Override
    public Object visit(ASTMethodDeclaration methodDeclaration, Object data) {
        for (ASTFormalParameter formalParameter : methodDeclaration.getFormalParameters()) {
            ASTType paramType = formalParameter.getTypeNode();
            if (isObjectMapper(paramType) && !hasAllowedAnnotations(methodDeclaration)) {
                asCtx(data).addViolation(formalParameter);
            }
        }
        return super.visit(methodDeclaration, data);
    }

    @Override
    public Object visit(ASTLocalVariableDeclaration node, Object data) {
        ASTType variableType = node.getTypeNode();
        if (isObjectMapper(variableType) && isInNotAllowedContext(node)) {
            asCtx(data).addViolation(node);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTConstructorCall constructorCall, Object data) {
        ASTClassType typeNode = constructorCall.getTypeNode();
        if (isObjectMapper(typeNode) && isInNotAllowedContext(constructorCall)) {
            asCtx(data).addViolation(constructorCall);
        }

        return super.visit(constructorCall, data);
    }

    private boolean isObjectMapper(TypeNode typeNode) {
        if (typeNode != null) {
            return objectMapperClasses.stream().anyMatch(className -> isTypeMatches(typeNode, className));
        }
        return false;
    }

    private boolean isTypeMatches(TypeNode typeNode, String className) {
        return getProperty(CHECK_SUBTYPES_DESCRIPTOR)
            ? TypeTestUtil.isA(className, typeNode)
            : TypeTestUtil.isExactlyA(className, typeNode);
    }

    /**
     * Checks if an allowed annotation is not presented and
     * if ObjectMapper is not declared in fields, constructors, and initialization blocks.
     */
    private boolean isInNotAllowedContext(Node node) {
        return node.ancestors(ASTMethodDeclaration.class).toStream().noneMatch(this::hasAllowedAnnotations)
            && node.ancestors(ASTFieldDeclaration.class).isEmpty()
            && node.ancestors(ASTConstructorDeclaration.class).isEmpty()
            && node.ancestors(ASTInitializer.class).isEmpty();
    }

    /**
     * Check if the ObjectMapper is used in a method with specific annotations.
     */
    private boolean hasAllowedAnnotations(ASTMethodDeclaration method) {
        return method.isAnyAnnotationPresent(annotations);
    }
}

