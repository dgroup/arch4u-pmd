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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * MDC needs to be cleaned in every method.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/TBD">https://github.com/dgroup/arch4u-pmd/issues/TBD</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class PotentiallyThreadLocalPollutionByMdc extends AbstractJavaRule {

    /**
     * Property descriptor.
     */
    private static final PropertyDescriptor<List<String>> MDC_CLASSES =
        PropertyFactory.stringListProperty("mdcClasses")
            .desc("List of the MDC classes")
            .emptyDefaultValue()
            .build();

    /**
     * Map to save MDC keys and expression nodes.
     */
    private final Map<String, ASTPrimaryExpression> keymap;

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public PotentiallyThreadLocalPollutionByMdc() {
        this.keymap = new HashMap<>();
        this.definePropertyDescriptor(MDC_CLASSES);
    }

    @Override
    public Object visit(final ASTMethodDeclaration mthd, final Object data) {
        mthd.findDescendantsOfType(ASTPrimaryPrefix.class)
            .stream()
            .filter(this::isMdc)
            .forEach(this::updateMap);
        for (final ASTPrimaryExpression expression : this.keymap.values()) {
            asCtx(data).addViolation(expression);
        }
        return data;
    }

    /**
     * Checks if the provided node is MDC class.
     * @param prefix Prefix node.
     * @return True if the provided node is MDC class.
     */
    private boolean isMdc(final ASTPrimaryPrefix prefix) {
        return this.getProperty(MDC_CLASSES)
            .stream()
            .anyMatch(mdc -> TypeTestUtil.isA(mdc, prefix));
    }

    /**
     * Perform the {@code keymap} map update.
     * If MDC has {@code put} invocation, it will save key and expression.
     * If it has {@code remove} invocation, it will remove value by the key.
     * The map must be cleared on the {@code MDC.clear()} invocation.
     * @param prefix Prefix node.
     */
    private void updateMap(final ASTPrimaryPrefix prefix) {
        final String method = prefix.getChild(0).getImage();
        final ASTPrimaryExpression parent = prefix.getFirstParentOfType(ASTPrimaryExpression.class);
        final String key = getKeyImage(parent);
        if (method.endsWith(".put")) {
            this.keymap.put(key, parent);
        } else if (method.endsWith(".remove")) {
            this.keymap.remove(key);
        } else if (method.endsWith(".clear")) {
            this.keymap.clear();
        }
    }

    /**
     * Returns MDC key.
     * @param node Image holding node.
     * @return String of the MDC key.
     */
    private static String getKeyImage(final JavaNode node) {
        return Optional.ofNullable(node.getFirstChildOfType(ASTPrimarySuffix.class))
            .map(suffix -> suffix.getFirstChildOfType(ASTArguments.class))
            .map(args -> args.getFirstChildOfType(ASTArgumentList.class))
            .map(list -> list.getFirstChildOfType(ASTExpression.class))
            .map(expr -> expr.getFirstChildOfType(ASTPrimaryExpression.class))
            .map(prex -> prex.getFirstChildOfType(ASTPrimaryPrefix.class))
            .map(prefix -> prefix.getFirstChildOfType(ASTName.class))
            .map(AbstractNode::getImage)
            .orElse(null);
    }
}
