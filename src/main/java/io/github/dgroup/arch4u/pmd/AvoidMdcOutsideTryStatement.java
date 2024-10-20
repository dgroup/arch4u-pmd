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
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFinallyClause;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * A PMD rule that prohibits the invocation of specified methods from a given class.
 *
 * @checkstyle ReturnCountCheck (100 lines)
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/22">https://github.com/dgroup/arch4u-pmd/issues/22</a>
 * @since 0.1.0
 */
@SuppressWarnings({
    "PMD.OnlyOneReturn",
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public final class AvoidMdcOutsideTryStatement extends AbstractJavaRule {

    /**
     * Property Descriptor for MDC classes.
     */
    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("mdcClasses")
            .desc("Full name of the MDC classes. Use a comma (,) as a delimiter.")
            .defaultValues("org.slf4j.MDC")
            .build();

    /**
     * Property Descriptor for method names.
     */
    private static final PropertyDescriptor<List<String>> TRY =
        PropertyFactory.stringListProperty("tryMethodNames")
            .desc("Method names that should be within a Try statement.")
            .defaultValues("put")
            .build();

    /**
     * Property Descriptor for method names.
     */
    private static final PropertyDescriptor<List<String>> FINALLY =
        PropertyFactory.stringListProperty("finallyMethodNames")
            .desc("Method names that should be within a Finally clause.")
            .defaultValues("remove", "clear")
            .build();

    /**
     * Constructor.
     */
    public AvoidMdcOutsideTryStatement() {
        definePropertyDescriptor(CLASSES);
        definePropertyDescriptor(TRY);
        definePropertyDescriptor(FINALLY);
    }

    @Override
    public Object visit(final ASTMethodCall node, final Object data) {
        if (this.isMdc(node.getQualifier()) && (this.inTry(node) || this.inFinally(node))) {
            asCtx(data).addViolation(node);
        }
        return data;
    }

    /**
     * Checks if the provided expression is an invocation of an MDC class.
     *
     * @param qualifier An expression.
     * @return Result if the expression is an MDC invocation.
     */
    @SuppressWarnings("AvoidInlineConditionals")
    private boolean isMdc(final ASTExpression qualifier) {
        return qualifier != null && this.getProperty(CLASSES)
            .stream()
            .anyMatch(mdcClass -> TypeTestUtil.isA(mdcClass, qualifier.getTypeMirror()));
    }

    /**
     * Checks if the method invocation located inside a try-statement.
     *
     * @param node Method invocation node.
     * @return True if is inside a try-statement.
     */
    private boolean inTry(final ASTMethodCall node) {
        return this.getProperty(TRY).contains(node.getMethodName())
            && node.ancestors(ASTTryStatement.class).isEmpty();
    }

    /**
     * Checks if the method invocation located inside a finally-statement.
     *
     * @param node Method invocation node.
     * @return True if is inside a finally-statement.
     */
    private boolean inFinally(final ASTMethodCall node) {
        return this.getProperty(FINALLY).contains(node.getMethodName())
            && node.ancestors(ASTFinallyClause.class).isEmpty();
    }
}
