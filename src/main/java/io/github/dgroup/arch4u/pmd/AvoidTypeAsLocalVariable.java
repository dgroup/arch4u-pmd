package io.github.dgroup.arch4u.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import java.util.List;

@SuppressWarnings({
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public class AvoidTypeAsLocalVariable extends AbstractJavaRule {

    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("classes")
            .desc("Full names of the classes")
            // @todo #/DEV Remove default value for classes, use *.xml configuration instead
            .defaultValues("com.fasterxml.jackson.databind.ObjectMapper")
            .build();

    private static final PropertyDescriptor<List<String>> ANNOTATIONS =
        PropertyFactory.stringListProperty("annotations")
            .desc("Full name of the method annotations in which it's allowed to use objects")
            // @todo #/DEV Remove default value for methods, use *.xml configuration instead
            .defaultValues(
                "javax.annotation.PostConstruct",
                "org.springframework.context.annotation.Bean"
            )
            .build();

    private static final PropertyDescriptor<Boolean> SUBTYPES =
        PropertyFactory.booleanProperty("checkSubtypes")
            .desc("The property matches whether the classes subtypes should be checked")
            .defaultValue(true)
            .build();

    private final List<String> classes;
    private final List<String> annotations;

    public AvoidTypeAsLocalVariable() {
        definePropertyDescriptor(CLASSES);
        definePropertyDescriptor(ANNOTATIONS);
        definePropertyDescriptor(SUBTYPES);
        this.classes = getProperty(CLASSES);
        this.annotations = getProperty(ANNOTATIONS);
    }

    @Override
    public Object visit(final ASTMethodDeclaration method, final Object data) {
        for (final ASTFormalParameter param : method.getFormalParameters()) {
            if (this.illegalType(param.getTypeNode()) && !this.hasAllowedAnnotations(method)) {
                asCtx(data).addViolation(param);
            }
        }
        return super.visit(method, data);
    }

    @Override
    public Object visit(final ASTLocalVariableDeclaration node, final Object data) {
        if (this.illegalType(node.getTypeNode()) && this.isInNotAllowedContext(node)) {
            asCtx(data).addViolation(node);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(final ASTConstructorCall ctor, final Object data) {
        if (this.illegalType(ctor.getTypeNode()) && this.isInNotAllowedContext(ctor)) {
            asCtx(data).addViolation(ctor);
        }
        return super.visit(ctor, data);
    }

    private boolean illegalType(final TypeNode type) {
        boolean found = false;
        if (type != null) {
            found = this.classes.stream().anyMatch(className -> isTypeMatches(type, className));
        }
        return found;
    }

    private boolean isTypeMatches(final TypeNode type, final String classname) {
        return getProperty(SUBTYPES)
            ? TypeTestUtil.isA(classname, type)
            : TypeTestUtil.isExactlyA(classname, type);
    }

    /**
     * Checks if an allowed annotation is not presented and
     * if type is not declared in fields, constructors, and initialization blocks.
     */
    private boolean isInNotAllowedContext(final Node node) {
        return node.ancestors(ASTMethodDeclaration.class).toStream().noneMatch(this::hasAllowedAnnotations)
            && node.ancestors(ASTFieldDeclaration.class).isEmpty()
            && node.ancestors(ASTConstructorDeclaration.class).isEmpty()
            && node.ancestors(ASTInitializer.class).isEmpty();
    }

    /**
     * Check if the type is used in a method with specific annotations.
     */
    private boolean hasAllowedAnnotations(final ASTMethodDeclaration method) {
        return method.isAnyAnnotationPresent(annotations);
    }
}
