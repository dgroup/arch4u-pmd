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

/**
 * A PMD rule that prohibits the usage of specified classes.
 *
 * @since 0.1.0
 */
@SuppressWarnings({
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public final class AvoidProhibitedClasses extends AbstractJavaRule {

    /**
     * Property descriptor for the fully qualified name of the class whose methods are prohibited.
     */
    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("fullNames")
            .desc("Fully qualified class names")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor for whether subtype checking is enabled.
     */
    private static final PropertyDescriptor<Boolean> SUBTYPES =
        PropertyFactory.booleanProperty("checkSubtypes")
            .desc("The property matches whether the subtypes should be checked")
            .defaultValue(false)
            .build();

    public AvoidProhibitedClasses() {
        definePropertyDescriptor(CLASSES);
        definePropertyDescriptor(SUBTYPES);
    }

    @Override
    public Object visit(final ASTClassType node, final Object data) {
        this.getProperty(CLASSES)
            .stream()
            .filter(this.getProperty(SUBTYPES)
                        ? name -> TypeTestUtil.isA(name, node)
                        : name -> TypeTestUtil.isExactlyA(name, node))
            .findFirst()
            .ifPresent(className -> asCtx(data).addViolation(node, className));
        return data;
    }
}
