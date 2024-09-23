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

import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFinallyClause;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;

/**
 * A PMD rule that prohibits the invocation of specified methods from a given class.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/22">https://github.com/dgroup/arch4u-pmd/issues/22</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class AvoidMdcOutsideTryStatement extends AbstractJavaRule {

    private static final PropertyDescriptor<List<String>> MDC_CLASSES_DESCRIPTOR =
        PropertyFactory.stringListProperty("mdcClasses")
            .desc("Full name of the MDC classes. Use a comma (,) as a delimiter.")
            .defaultValues("org.slf4j.MDC")
            .build();

    private static final PropertyDescriptor<List<String>> TRY_METHOD_NAMES_DESCRIPTOR =
        PropertyFactory.stringListProperty("tryMethodNames")
            .desc("Method names that should be within a Try statement.")
            .defaultValues("put")
            .build();

    private static final PropertyDescriptor<List<String>> FINALLY_METHOD_NAMES_DESCRIPTOR =
        PropertyFactory.stringListProperty("finallyMethodNames")
            .desc("Method names that should be within a Finally clause.")
            .defaultValues("remove", "clear")
            .build();

    public AvoidMdcOutsideTryStatement() {
        definePropertyDescriptor(MDC_CLASSES_DESCRIPTOR);
        definePropertyDescriptor(TRY_METHOD_NAMES_DESCRIPTOR);
        definePropertyDescriptor(FINALLY_METHOD_NAMES_DESCRIPTOR);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        ASTExpression qualifier = node.getQualifier();
        if (isMdc(qualifier)) {
            if (isViolationInTryStatement(node) || isViolationInFinallyStatement(node)) {
                asCtx(data).addViolation(node);
            }
        }
        return data;
    }

    private boolean isMdc(ASTExpression qualifier) {
        if (qualifier == null) {
            return false;
        }
        JTypeMirror type = qualifier.getTypeMirror();
        return getProperty(MDC_CLASSES_DESCRIPTOR).stream()
            .anyMatch(mdcClass -> TypeTestUtil.isA(mdcClass, type));
    }

    private boolean isViolationInTryStatement(ASTMethodCall node) {
        String methodName = node.getMethodName();
        return getProperty(TRY_METHOD_NAMES_DESCRIPTOR).contains(methodName)
            && isNotInTryStatement(node);
    }

    private boolean isNotInTryStatement(ASTMethodCall node) {
        return node.ancestors(ASTTryStatement.class).isEmpty();
    }

    private boolean isViolationInFinallyStatement(ASTMethodCall node) {
        String methodName = node.getMethodName();
        return getProperty(FINALLY_METHOD_NAMES_DESCRIPTOR).contains(methodName)
            && isNotInFinallyStatement(node);
    }

    private boolean isNotInFinallyStatement(ASTMethodCall node) {
        return node.ancestors(ASTFinallyClause.class).isEmpty();
    }

}
