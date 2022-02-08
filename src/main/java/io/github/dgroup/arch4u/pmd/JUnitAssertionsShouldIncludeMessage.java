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

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * A rule that prohibits to use JUnit assertion calls without message.
 * Also, see {@link AbstractJavaRule}.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/48">https://github.com/dgroup/arch4u-pmd/issues/48</a>
 * @since 0.1.0
 */
@SuppressWarnings({"PMD.StaticAccessToStaticFields", "PMD.ConstructorShouldDoInitialization"})
public final class JUnitAssertionsShouldIncludeMessage extends AbstractJavaRule {

    /**
     * Property descriptor with the list of the JUnit packages.
     */
    private static final PropertyDescriptor<List<String>> PCKGS =
        PropertyFactory.stringListProperty("packages")
            .desc("The junit package")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor with the list of the JUnit methods.
     */
    private static final PropertyDescriptor<List<String>> METHODS =
        PropertyFactory.stringListProperty("methods")
            .desc("List of prohibited methods")
            .emptyDefaultValue()
            .build();

    /**
     * List of assertion calls for methods arguments check.
     */
    private final List<AssertionCall> checks = new ArrayList<>(10);

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public JUnitAssertionsShouldIncludeMessage() {
        this.definePropertyDescriptor(PCKGS);
        this.definePropertyDescriptor(METHODS);
    }

    @Override
    public Object visit(final ASTArguments node, final Object data) {
        this.fillChecks();
        if (this.hasJunit(node)) {
            for (final AssertionCall call : this.checks) {
                if (call.checkArgsNumInCall(node)) {
                    this.addViolation(data, node);
                }
            }
        }
        return data;
    }

    /**
     * Method to fill the list with assertion calls from property.
     */
    private void fillChecks() {
        final List<String> methods = this.getProperty(METHODS);
        for (final String mtd : methods) {
            final String[] split = mtd.split("-");
            this.checks.add(new AssertionCall(split[0], Integer.parseInt(split[1])));
        }
    }

    /**
     * Checks if class has JUnit in imports.
     *
     * @param node Method arguments.
     * @return True if the occurrence contains prohibited method.
     */
    private boolean hasJunit(final ASTArguments node) {
        return node.getFirstParentOfType(ASTCompilationUnit.class)
            .findChildrenOfType(ASTImportDeclaration.class)
            .stream()
            .anyMatch(
                el -> this.getProperty(PCKGS).stream().anyMatch(
                    el.getImportedName()::startsWith
                )
            );
    }

    /**
     * Class to store and check assertion calls.
     * @since 0.1.0
     */
    private static class AssertionCall {
        /**
         * Assertion call name.
         */
        private final String name;

        /**
         * Assertion call arguments count.
         */
        private final int cnt;

        /**
         * Ctor.
         *
         * @param name Assertion call name.
         * @param cnt Assertion call arguments count.
         */
        AssertionCall(final String name, final int cnt) {
            this.cnt = cnt;
            this.name = name;
        }

        /**
         * Check if there is an assertion call with incorrect argument amount.
         *
         * @checkstyle NestedIfDepthCheck (200 lines)
         * @param node Node with assertion call arguments.
         * @return Node name if everything is correct, null if there is a violation.
         */
        @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
        public boolean checkArgsNumInCall(final ASTArguments node) {
            final String asrt = "Assert.";
            boolean res = false;
            if (node.size() == this.cnt
                && node.getNthParent(2) instanceof ASTPrimaryExpression
            ) {
                final ASTPrimaryPrefix prefix = node.getNthParent(2)
                    .getFirstChildOfType(ASTPrimaryPrefix.class);
                if (prefix != null) {
                    final ASTName val = prefix.getFirstChildOfType(ASTName.class);
                    if (val != null
                        && (val.hasImageEqualTo(this.name)
                        || val.getImage().endsWith(asrt + this.name))
                    ) {
                        res = true;
                    }
                }
            }
            return res;
        }
    }
}
