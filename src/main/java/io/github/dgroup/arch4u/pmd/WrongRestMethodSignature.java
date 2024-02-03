/*
 * MIT License
 *
 * Copyright (c) 2019-2022 Yurii Dubinka
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
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * The rule checks if the method with the REST endpoint annotation
 * has {@code public} access modifier and {@link java.lang.Override} annotation.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/54">https://github.com/dgroup/arch4u-pmd/issues/54</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class WrongRestMethodSignature extends AbstractJavaRule {

    /**
     * Property descriptor.
     */
    private static final PropertyDescriptor<List<String>> REST_ANNOTS =
        PropertyFactory.stringListProperty("restAnnotations")
            .desc("List of the REST endpoint method annotations")
            .emptyDefaultValue()
            .build();

    /**
     * Constructor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public WrongRestMethodSignature() {
        this.definePropertyDescriptor(REST_ANNOTS);
    }

    @Override
    public Object visit(final ASTMethodDeclaration mthd, final Object data) {
        if (this.isRestMethod(mthd) && hasWrongSignature(mthd)) {
            asCtx(data).addViolation(mthd);
        }
        return super.visit(mthd, data);
    }

    /**
     * Finds method REST endpoint annotations. Returns the first one.
     * @param mthd Method declaration node.
     * @return Optional of the first declared REST endpoint annotation.
     */
    private boolean isRestMethod(final ASTMethodDeclaration mthd) {
        return mthd.isAnyAnnotationPresent(this.getProperty(REST_ANNOTS));
    }

    /**
     * Checks if the REST endpoint method has prohibited signature.
     * Correct method's signature has public access modifier
     * and {@link java.lang.Override} annotation.
     * @param mthd Method declaration node.
     * @return True if the method has prohibited signature.
     */
    private static boolean hasWrongSignature(final ASTMethodDeclaration mthd) {
        return !mthd.isAnnotationPresent("java.lang.Override") || !mthd.isPublic();
    }
}
