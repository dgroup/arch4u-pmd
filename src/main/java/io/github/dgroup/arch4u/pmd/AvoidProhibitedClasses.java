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

import net.sourceforge.pmd.lang.java.ast.ASTClassType;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * A PMD rule that prohibits the usage of specified classes.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class AvoidProhibitedClasses extends AbstractJavaRule {

    /**
     * Property descriptor for the fully qualified name of the class whose methods are prohibited.
     */
    private static final PropertyDescriptor<List<String>> CLASS_NAME_DESCRIPTOR =
            PropertyFactory.stringListProperty("fullNames")
                    .desc("Fully qualified class names")
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

    public AvoidProhibitedClasses() {
        definePropertyDescriptor(CLASS_NAME_DESCRIPTOR);
        definePropertyDescriptor(CHECK_SUBTYPES_DESCRIPTOR);
    }

    @Override
    public Object visit(ASTClassType node, Object data) {
        List<String> prohibitedClassNames = getProperty(CLASS_NAME_DESCRIPTOR);
        boolean checkSubtypes = getProperty(CHECK_SUBTYPES_DESCRIPTOR);

        Predicate<String> checkTypeFunction = checkSubtypes
                ? className -> TypeTestUtil.isA(className, node)
                : className -> TypeTestUtil.isExactlyA(className, node);

        checkForViolations(prohibitedClassNames, checkTypeFunction, node, data);
        return data;
    }

    private void checkForViolations(List<String> prohibitedClassNames,
                                    Predicate<String> checkTypeFunction,
                                    ASTClassType node,
                                    Object data
    ) {
        prohibitedClassNames.stream()
                .filter(checkTypeFunction)
                .findFirst()
                .ifPresent(className -> asCtx(data).addViolation(node, className));
    }
}