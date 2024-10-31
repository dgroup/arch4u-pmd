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
import java.util.Objects;
import java.util.Optional;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * This rule checks if the methods with the REST endpoint annotations
 * (e.g. {@code org.springframework.web.bind.annotation.GetMapping}
 * have any mandatory metric annotations (e.g. {@code io.micrometer.core.annotation.Timed}).
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/28">https://github.com/dgroup/arch4u-pmd/issues/28</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class MissingMandatoryAnnotation extends AbstractJavaRule {

    /**
     * Property descriptor.
     */
    private static final PropertyDescriptor<List<String>> REST_ANNOTS =
        PropertyFactory.stringListProperty("restAnnotations")
            .desc("List of the REST endpoint method annotations")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor.
     */
    private static final PropertyDescriptor<List<String>> MANDATORY_ANNOTS =
        PropertyFactory.stringListProperty("mandatoryAnnotations")
            .desc("List of the mandatory metric method annotations")
            .emptyDefaultValue()
            .build();

    /**
     * Constructor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public MissingMandatoryAnnotation() {
        this.definePropertyDescriptor(REST_ANNOTS);
        this.definePropertyDescriptor(MANDATORY_ANNOTS);
    }

    @Override
    public Object visit(final ASTMethodDeclaration mthd, final Object data) {
        final Optional<ASTAnnotation> restannot = this.getRestAnnotation(mthd);
        if (restannot.isPresent() && this.hasNoMandatoryAnnotation(mthd)) {
            asCtx(data).addViolation(restannot.get());
        }
        return super.visit(mthd, data);
    }

    /**
     * Checks if the method has no mandatory metric annotations.
     *
     * @param mthd Method declaration node.
     * @return True if there is no mandatory metric annotations.
     */
    private boolean hasNoMandatoryAnnotation(final ASTMethodDeclaration mthd) {
        return !mthd.isAnyAnnotationPresent(this.getProperty(MANDATORY_ANNOTS));
    }

    /**
     * Finds method REST endpoint annotations. Returns the first one.
     *
     * @param mthd Method declaration node.
     * @return Optional of the first declared REST endpoint annotation.
     */
    private Optional<ASTAnnotation> getRestAnnotation(final ASTMethodDeclaration mthd) {
        return this.getProperty(REST_ANNOTS)
            .stream()
            .map(mthd::getAnnotation)
            .filter(Objects::nonNull)
            .findFirst();
    }
}
