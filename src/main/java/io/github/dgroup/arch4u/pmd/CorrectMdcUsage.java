/*
 * MIT License
 *
 * Copyright (c) 2019-2022 Yurii Dubinka
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
import java.util.Objects;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.xpath.TypeIsExactlyFunction;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * Rule to prohibit MDC usage not in try with resources.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/24">https://github.com/dgroup/arch4u-pmd/issues/24</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class CorrectMdcUsage extends AbstractJavaRule {

    /**
     * Property descriptor with list of classes.
     */
    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("classes")
            .desc("List of class names")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor with list of specified interfaces and classes methods.
     */
    private static final PropertyDescriptor<List<String>> METHODS =
        PropertyFactory.stringListProperty("methods")
            .desc("List of method names")
            .emptyDefaultValue()
            .build();

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public CorrectMdcUsage() {
        this.definePropertyDescriptor(CLASSES);
        this.definePropertyDescriptor(METHODS);
    }

    @Override
    public Object visit(final ASTMethodDeclaration node, final Object data) {
        if (this.hasMdcType(node)
            && this.mdcIsNotInTry(node)
            && this.mdcNotUsesRemoveMethod(node)
        ) {
            this.addViolation(data, node);
        }
        return super.visit(node, data);
    }

    /**
     * Method to check if class uses MDC api.
     *
     * @param node ASTMethodDeclaration node of AST.
     * @return True if class uses MDC.
     */
    private boolean hasMdcType(final ASTMethodDeclaration node) {
        return node.getFirstParentOfType(ASTCompilationUnit.class)
            .findChildrenOfType(ASTImportDeclaration.class)
            .stream()
            .map(ASTImportDeclaration::getImportedName)
            .anyMatch(el -> this.getProperty(CLASSES).stream().anyMatch(el::contains));
    }

    /**
     * Method to check if class uses MDC api in try with resources statement.
     *
     * @param node ASTMethodDeclaration node of AST.
     * @return True if class uses MDC in try with resources statement.
     */
    private boolean mdcIsNotInTry(final ASTMethodDeclaration node) {
        return node.findDescendantsOfType(ASTClassOrInterfaceType.class)
            .stream()
            .filter(el -> this.getProperty(CLASSES).contains(el.getImage()))
            .anyMatch(el -> el.getFirstParentOfType(ASTTryStatement.class) == null);
    }

    /**
     * Method to check if class uses MDC api with remove method.
     *
     * @param node ASTMethodDeclaration node of AST.
     * @return True if class uses MDC uses remove method.
     */
    private boolean mdcNotUsesRemoveMethod(final ASTMethodDeclaration node) {
        return node.findDescendantsOfType(ASTClassOrInterfaceType.class)
            .stream()
            .filter(
                el -> this.getProperty(CLASSES).stream()
                    .anyMatch(cls -> TypeIsExactlyFunction.typeIsExactly(el, cls))
            ).map(type -> type.getFirstParentOfType(ASTPrimaryExpression.class))
            .filter(Objects::nonNull)
            .map(type -> type.getFirstDescendantOfType(ASTPrimarySuffix.class))
            .filter(Objects::nonNull)
            .map(AbstractNode::getImage)
            .noneMatch(el -> this.getProperty(METHODS).contains(el));
    }
}
