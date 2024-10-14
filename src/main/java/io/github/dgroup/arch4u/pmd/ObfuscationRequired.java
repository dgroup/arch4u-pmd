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
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * A rule that prohibits the using methods of a particular class.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/22">https://github.com/dgroup/arch4u-pmd/issues/22</a>
 * @since 0.1.0
 */
@SuppressWarnings({"PMD.OnlyOneReturn", "PMD.StaticAccessToStaticFields"})
public final class ObfuscationRequired extends AbstractJavaRule {

    /**
     * Property descriptor with test class suffix.
     */
    private static final PropertyDescriptor<List<String>> LOGGERS =
        PropertyFactory.stringListProperty("loggerClasses")
            .desc("The fully qualified name of the class for logging")
            .defaultValues(
                "org.slf4j.Logger",
                "java.util.logging.Logger",
                "org.apache.log4j.Logger",
                "org.apache.logging.log4j.Logger"
            )
            .build();

    /**
     * Property descriptor with the list of the prohibited classes.
     */
    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("sensitiveClasses")
            .desc("List of prohibited methods")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor with the list of the prohibited packages.
     */
    private static final PropertyDescriptor<List<String>> PACKAGES =
        PropertyFactory.stringListProperty("sensitivePackages")
            .desc("List of prohibited packages")
            .emptyDefaultValue()
            .build();

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public ObfuscationRequired() {
        this.definePropertyDescriptor(LOGGERS);
        this.definePropertyDescriptor(CLASSES);
        this.definePropertyDescriptor(PACKAGES);
    }

    @Override
    public Object visit(final ASTMethodCall node, final Object data) {
        if (node.getMethodType() != null && this.isLoggerMethod(node.getMethodType())) {
            node.getArguments()
                .toStream()
                .filter(this::isSensitiveObject)
                .forEach(arg -> asCtx(data).addViolation(arg));
        }
        return data;
    }

    /**
     * Checks if the method signature is a logger method.
     * @param signature A method signature node.
     * @return True if it's a logger method.
     */
    private boolean isLoggerMethod(final JMethodSig signature) {
        return this.getProperty(LOGGERS)
            .stream()
            .anyMatch(logger -> TypeTestUtil.isA(logger, signature.getDeclaringType()));
    }

    /**
     * Checks if the expression is a sensitive objects
     * that should be obfuscated.
     * @param arg An expression.
     * @return True if it's a sensitive object.
     * @checkstyle AvoidInlineConditionalsCheck (10 lines)
     */
    private boolean isSensitiveObject(final ASTExpression arg) {
        return arg instanceof ASTMethodCall
            ? this.hasSensitiveType((ASTMethodCall) arg)
            : this.isSensitiveType(arg.getTypeMirror());
    }

    /**
     * Checks if the method argument has a sensitive type.
     * If the method is toString(), check the type of the object it's called on.
     * Example: {@code person.toString()}.
     * Otherwise, check if the method returns a sensitive type.
     * Example: {@code order.getPerson()}.
     * @param call A method invocation node.
     * @return True if there is a sensitive type.
     * @checkstyle ReturnCountCheck (10 lines)
     */
    private boolean hasSensitiveType(final ASTMethodCall call) {
        if ("toString".equals(call.getMethodType().getName())) {
            final ASTExpression qualifier = call.getQualifier();
            if (qualifier != null && this.isSensitiveType(qualifier.getTypeMirror())) {
                return true;
            }
        }
        return this.isSensitiveType(call.getMethodType().getReturnType());
    }

    /**
     * Checks if there is a sensitive type.
     * @param type A type node.
     * @return True if there is a sensitive type.
     */
    private boolean isSensitiveType(final JTypeMirror type) {
        return this.isSensitiveClass(type) || this.isSensitivePackage(type);
    }

    /**
     * Checks if there is a sensitive class type.
     * @param type A type node.
     * @return True if there is a sensitive type.
     */
    private boolean isSensitiveClass(final JTypeMirror type) {
        return this.getProperty(CLASSES)
            .stream()
            .anyMatch(sensitiveClass -> TypeTestUtil.isA(sensitiveClass, type));
    }

    /**
     * Checks if there is a package with sensitive objects.
     * @param type A type node.
     * @return True if there is a sensitive type.
     * @checkstyle ReturnCountCheck (10 lines)
     */
    private boolean isSensitivePackage(final JTypeMirror type) {
        if (type.getSymbol() == null) {
            return false;
        }
        final String pckg = type.getSymbol().getPackageName();
        return this.getProperty(PACKAGES)
            .stream()
            .anyMatch(pckg::startsWith);
    }
}
