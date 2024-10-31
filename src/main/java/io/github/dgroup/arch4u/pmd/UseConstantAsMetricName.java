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
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMemberValuePair;
import net.sourceforge.pmd.lang.java.ast.ASTStringLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * Use constant for metric name instead of hardcoded string literals.
 * @since 0.1.0
 */
@SuppressWarnings({
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public final class UseConstantAsMetricName extends AbstractJavaRule {

    /**
     * Property Descriptor for list of the metric annotations.
     */
    private static final PropertyDescriptor<List<String>> ANNOTATIONS =
        PropertyFactory.stringListProperty("metricAnnotations")
            .desc("List of the metric annotations.")
            .defaultValues("io.micrometer.core.annotation.Timed")
            .build();

    /**
     * List of the metric annotations.
     */
    private final List<String> annotations;

    /**
     * Constructor.
     */
    public UseConstantAsMetricName() {
        definePropertyDescriptor(ANNOTATIONS);
        this.annotations = getProperty(ANNOTATIONS);
    }

    @Override
    public Object visit(final ASTAnnotation annotation, final Object data) {
        if (this.isMetricAnnotation(annotation)) {
            annotation.getMembers()
                .map(ASTMemberValuePair::getValue)
                .filter(val -> val instanceof ASTStringLiteral)
                .forEach(val -> asCtx(data).addViolation(val));
        }
        return super.visit(annotation, data);
    }

    /**
     * Checks if the annotation is a metric annotation.
     * @param node An annotation node.
     * @return True if this is a metric annotation.
     */
    private boolean isMetricAnnotation(final ASTAnnotation node) {
        return this.annotations.stream()
            .anyMatch(annot -> TypeTestUtil.isExactlyA(annot, node));
    }
}
