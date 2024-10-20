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
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * A PMD rule that prohibits the invocation of specified methods from a given class.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/22">https://github.com/dgroup/arch4u-pmd/issues/22</a>
 * @since 0.1.0
 * @checkstyle ReturnCountCheck (150 lines)
 */
@SuppressWarnings({
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public final class AvoidProhibitedMethodsUsage extends AbstractJavaRule {

    /**
     * Property descriptor for the fully qualified name of the class whose methods are prohibited.
     */
    private static final PropertyDescriptor<String> CLASS =
        PropertyFactory.stringProperty("class")
            .desc("Fully qualified name of the class")
            .defaultValue("")
            .build();

    /**
     * Property descriptor for the names of methods that are prohibited to invoke.
     */
    private static final PropertyDescriptor<List<String>> METHODS =
        PropertyFactory.stringListProperty("methods")
            .desc("Name of the method to prohibit")
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

    /**
     * Constructor.
     */
    public AvoidProhibitedMethodsUsage() {
        definePropertyDescriptor(CLASS);
        definePropertyDescriptor(METHODS);
        definePropertyDescriptor(SUBTYPES);
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Object visit(final ASTMethodCall node, final Object data) {
        if (node.getMethodType() == null || node.getMethodType().getDeclaringType() == null) {
            return data;
        }
        if (this.getProperty(METHODS).contains(node.getMethodType().getName())) {
            if (this.getProperty(SUBTYPES)
                && this.isSubtype(node.getMethodType().getDeclaringType())) {
                asCtx(data).addViolation(
                    node, this.getProperty(CLASS), node.getMethodType().getName()
                );
            }
            if (this.isExactClass(node.getMethodType().getDeclaringType().toString())) {
                asCtx(data).addViolation(
                    node, this.getProperty(CLASS), node.getMethodType().getName()
                );
            }
        }
        return data;
    }

    /**
     * Checks if the class is prohibited.
     *
     * @param classname Class name.
     * @return True if the class is prohibited.
     */
    private boolean isExactClass(final String classname) {
        return this.getProperty(CLASS).equals(classname);
    }

    /**
     * Checks if the provided class node is a subtype of a prohibited class.
     *
     * @param type A class node.
     * @return True if this is a prohibited class.
     */
    private boolean isSubtype(final JTypeMirror type) {
        return TypeTestUtil.isA(this.getProperty(CLASS), type);
    }
}
