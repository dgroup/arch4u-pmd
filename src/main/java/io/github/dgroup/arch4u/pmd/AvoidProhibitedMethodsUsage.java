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

import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
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
public final class AvoidProhibitedMethodsUsage extends AbstractJavaRule {

    /**
     * Property descriptor for the fully qualified name of the class whose methods are prohibited.
     */
    private static final PropertyDescriptor<String> CLASS_NAME_DESCRIPTOR =
            PropertyFactory.stringProperty("class")
                    .desc("Fully qualified name of the class")
                    .defaultValue("")
                    .build();

    /**
     * Property descriptor for the names of methods that are prohibited to invoke.
     */
    private static final PropertyDescriptor<List<String>> METHOD_NAME_DESCRIPTOR =
            PropertyFactory.stringListProperty("methods")
                    .desc("Name of the method to prohibit")
                    .emptyDefaultValue()
                    .build();

    /**
     * Property descriptor for whether subtype checking is enabled.
     */
    private static final PropertyDescriptor<Boolean> CHECK_SUBTYPES_DESCRIPTOR =
            PropertyFactory.booleanProperty("checkSubtypes")
                    .desc("The property matches whether the subtypes should be checked")
                    .defaultValue(false)
                    .build();

    public AvoidProhibitedMethodsUsage() {
        definePropertyDescriptor(CLASS_NAME_DESCRIPTOR);
        definePropertyDescriptor(METHOD_NAME_DESCRIPTOR);
        definePropertyDescriptor(CHECK_SUBTYPES_DESCRIPTOR);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        String prohibitedClassName = getProperty(CLASS_NAME_DESCRIPTOR);
        List<String> prohibitedMethodNames = getProperty(METHOD_NAME_DESCRIPTOR);
        boolean checkSubtypes = getProperty(CHECK_SUBTYPES_DESCRIPTOR);

        JMethodSig methodSignature = node.getMethodType();
        if (methodSignature != null) {
            JTypeMirror declaringType = methodSignature.getDeclaringType();
            if (declaringType != null) {
                String className = declaringType.toString();
                String methodName = methodSignature.getName();
                if (prohibitedMethodNames.contains(methodName)) {
                    if (checkSubtypes) {
                        if (isSubtype(prohibitedClassName, declaringType)) {
                            asCtx(data).addViolation(node, prohibitedClassName, methodName);
                        }
                    } else if (isExactClass(prohibitedClassName, className)) {
                        asCtx(data).addViolation(node, prohibitedClassName, methodName);
                    }
                }
            }
        }
        return data;
    }

    private static boolean isExactClass(String prohibitedClassName, String className) {
        return prohibitedClassName.equals(className);
    }

    private static boolean isSubtype(String prohibitedClassName, JTypeMirror declaringType) {
        return TypeTestUtil.isA(prohibitedClassName, declaringType);
    }
}
