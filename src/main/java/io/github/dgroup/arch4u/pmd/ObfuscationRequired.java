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
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;

/**
 * A rule that prohibits the using methods of a particular class.
 *
 * @see
 * <a href="https://github.com/dgroup/arch4u-pmd/issues/22">https://github.com/dgroup/arch4u-pmd/issues/22</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class ObfuscationRequired extends AbstractJavaRule {

    /**
     * Property descriptor with test class suffix.
     */
    private static final PropertyDescriptor<List<String>> LOGGER_CLASSES_DESCRIPTOR =
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
    private static final PropertyDescriptor<List<String>> SENSITIVE_CLASSES_DESCRIPTOR =
        PropertyFactory.stringListProperty("sensitiveClasses")
            .desc("List of prohibited methods")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor with the list of the prohibited packages.
     */
    private static final PropertyDescriptor<List<String>> SENSITIVE_PACKAGES_DESCRIPTOR =
            PropertyFactory.stringListProperty("sensitivePackages")
                    .desc("List of prohibited packages")
                    .emptyDefaultValue()
                    .build();

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public ObfuscationRequired() {
        this.definePropertyDescriptor(LOGGER_CLASSES_DESCRIPTOR);
        this.definePropertyDescriptor(SENSITIVE_CLASSES_DESCRIPTOR);
        this.definePropertyDescriptor(SENSITIVE_PACKAGES_DESCRIPTOR);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        JMethodSig methodSignature = node.getMethodType();
        if (methodSignature != null) {
            if (isLoggerMethod(methodSignature)) {
                node.getArguments().forEach(arg -> {
                    if (isSensitiveObject(arg)) {
                        asCtx(data).addViolation(arg);
                    }
                });
            }
        }
        return data;
    }

    private boolean isLoggerMethod(JMethodSig methodSignature) {
        return getProperty(LOGGER_CLASSES_DESCRIPTOR).stream()
                .anyMatch(loggerClass -> TypeTestUtil.isA(loggerClass, methodSignature.getDeclaringType()));
    }

    private boolean isSensitiveObject(ASTExpression arg) {
        if (arg instanceof ASTMethodCall) {
            ASTMethodCall methodCall = (ASTMethodCall) arg;
            return hasSensitiveType(methodCall);
        } else {
            JTypeMirror argumentType = arg.getTypeMirror();
            return isSensitiveType(argumentType);
        }
    }

    /**
     * Checks if the method argument has a sensitive type.
     * If the method is toString(), check the type of the object it's called on. Example: {@code person.toString()}.
     * Otherwise, check if the method returns a sensitive type. Example: {@code order.getPerson()}.
     */
    private boolean hasSensitiveType(ASTMethodCall methodCall) {
        String methodName = methodCall.getMethodType().getName();

        if ("toString".equals(methodName)) {
            ASTExpression qualifier = methodCall.getQualifier();
            if (qualifier != null && isSensitiveType(qualifier.getTypeMirror())) {
                return true;
            }
        }

        JTypeMirror methodReturnType = methodCall.getMethodType().getReturnType();
        return isSensitiveType(methodReturnType);
    }

    private boolean isSensitiveType(JTypeMirror type) {
        return isSensitiveClass(type) || isSensitivePackage(type);
    }

    private boolean isSensitiveClass(JTypeMirror type) {
        return getProperty(SENSITIVE_CLASSES_DESCRIPTOR).stream()
                .anyMatch(sensitiveClass -> TypeTestUtil.isA(sensitiveClass, type));
    }

    private boolean isSensitivePackage(JTypeMirror type) {
        JTypeDeclSymbol typeSymbol = type.getSymbol();
        if (typeSymbol != null) {
            String packageName = typeSymbol.getPackageName();
            return getProperty(SENSITIVE_PACKAGES_DESCRIPTOR).stream()
                    .anyMatch(packageName::startsWith);
        }
        return false;
    }

}
