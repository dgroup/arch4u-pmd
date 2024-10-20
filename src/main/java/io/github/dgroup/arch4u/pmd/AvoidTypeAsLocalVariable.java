/*
 * MIT License
 *
 * Copyright (c) 2019-2024 Yurii Dubinka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.dgroup.arch4u.pmd;

import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTInitializer;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * The rule is designed to find a local variables that should be used
 * as a class fields such as {@link com.fasterxml.jackson.databind.ObjectMapper}.
 * @since 0.1.0
 */
@SuppressWarnings({
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public final class AvoidTypeAsLocalVariable extends AbstractJavaRule {

    /**
     * Property Descriptor for list of classes to find.
     */
    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("classes")
            .desc("Full names of the classes")
            // @todo #/DEV Remove default value for classes, use *.xml configuration instead
            .defaultValues("com.fasterxml.jackson.databind.ObjectMapper")
            .build();

    /**
     * Property Descriptor for list of annotations that allows to use the objects
     * as a local variables.
     */
    private static final PropertyDescriptor<List<String>> ANNOTATIONS =
        PropertyFactory.stringListProperty("annotations")
            .desc("Full name of the method annotations in which it's allowed to use objects")
            // @todo #/DEV Remove default value for methods, use *.xml configuration instead
            .defaultValues(
                "javax.annotation.PostConstruct",
                "org.springframework.context.annotation.Bean"
            )
            .build();

    /**
     * The property matches whether the classes subtypes should be checked.
     */
    private static final PropertyDescriptor<Boolean> SUBTYPES =
        PropertyFactory.booleanProperty("checkSubtypes")
            .desc("The property matches whether the classes subtypes should be checked")
            .defaultValue(true)
            .build();

    /**
     * Constructor.
     */
    public AvoidTypeAsLocalVariable() {
        this.definePropertyDescriptor(CLASSES);
        this.definePropertyDescriptor(ANNOTATIONS);
        this.definePropertyDescriptor(SUBTYPES);
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

    /**
     * Checks if the provided type is an illegal type.
     * @param type A variable type.
     * @return True if the type is illegal.
     */
    private boolean illegalType(final TypeNode type) {
        boolean found = false;
        if (type != null) {
            found = this.getProperty(CLASSES)
                .stream()
                .anyMatch(className -> this.isTypeMatches(type, className));
        }
        return found;
    }

    /**
     * Compares the type node with the class name.
     * @param type Type node.
     * @param classname Class name.
     * @return True if matches.
     * @checkstyle AvoidInlineConditionalsCheck (10 lines)
     */
    private boolean isTypeMatches(final TypeNode type, final String classname) {
        return this.getProperty(SUBTYPES)
            ? TypeTestUtil.isA(classname, type)
            : TypeTestUtil.isExactlyA(classname, type);
    }

    /**
     * Checks if an allowed annotation is not presented and
     * if type is not declared in fields, constructors, and initialization blocks.
     * @param node A node to check.
     * @return True if the node is in a prohibited context.
     */
    private boolean isInNotAllowedContext(final Node node) {
        return node.ancestors(ASTMethodDeclaration.class).toStream()
            .noneMatch(this::hasAllowedAnnotations)
            && node.ancestors(ASTFieldDeclaration.class).isEmpty()
            && node.ancestors(ASTConstructorDeclaration.class).isEmpty()
            && node.ancestors(ASTInitializer.class).isEmpty();
    }

    /**
     * Check if the type is used in a method with specific annotations.
     * @param method A method declaration node.
     * @return True if an allowed annotation is presented.
     */
    private boolean hasAllowedAnnotations(final ASTMethodDeclaration method) {
        return method.isAnyAnnotationPresent(this.getProperty(ANNOTATIONS));
    }
}
