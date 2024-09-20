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

import java.util.regex.Pattern;

import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.reporting.RuleContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Rule to avoid creating string constants for {@code MediaType} values.
 * Example: {@code "application/json'}
 * Use existing classes.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/discussions/43">https://github.com/dgroup/arch4u-pmd/discussions/43</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class UseExistingConstant extends AbstractJavaRule {

    /**
     * Property descriptor with regexp of prohibited string.
     */
    private static final PropertyDescriptor<String> REGEX_PROPERTY =
        PropertyFactory.stringProperty("regexPattern")
            .desc("Regular expression of prohibited string")
            .defaultValue(StringUtils.EMPTY)
            .build();

    /**
     * Pattern to match string text/literal that should be replaced by constant.
     */
    private Pattern pattern;

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public UseExistingConstant() {
        this.definePropertyDescriptor(REGEX_PROPERTY);
        this.addRuleChainVisit(ASTLiteral.class);
    }

    @Override
    public void start(final RuleContext ctx) {
        super.start(ctx);
        this.pattern = Pattern.compile(this.getProperty(REGEX_PROPERTY));
    }

    @Override
    public Object visit(final ASTLiteral node, final Object data) {
        if (node.isStringLiteral()) {
            String image = node.getTextBlockContent();
            image = image.substring(1, image.length() - 1);
            if (image.length() > 0 && this.pattern.matcher(image).find()) {
                asCtx(data).addViolation(node);
            }
        }
        return data;
    }
}
