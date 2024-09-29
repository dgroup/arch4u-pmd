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
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MDC needs to be cleaned in every method.
 *
 * @see <a href="https://github.com/dgroup/arch4u-pmd/issues/TBD">https://github.com/dgroup/arch4u-pmd/issues/TBD</a>
 * @since 0.1.0
 */
@SuppressWarnings({
    "PMD.OnlyOneReturn",
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorShouldDoInitialization",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public class PotentiallyThreadLocalPollutionByMdc extends AbstractJavaRule {

    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("mdcClasses")
            .desc("Full name of the MDC classes.")
            .defaultValues("org.slf4j.MDC")
            .build();

    private static final PropertyDescriptor<List<String>> PUT_METHODS =
        PropertyFactory.stringListProperty("putMethods")
            .desc("Names of methods that add entries to the MDC.")
            .defaultValues("put")
            .build();

    private static final PropertyDescriptor<List<String>> REMOVE_METHODS =
        PropertyFactory.stringListProperty("removeMethods")
            .desc("Names of methods that remove the entries by key from the MDC.")
            .defaultValues("remove")
            .build();

    private static final PropertyDescriptor<List<String>> CLEAR_METHODS =
        PropertyFactory.stringListProperty("clearMethods")
            .desc("Names of methods that clear the MDC.")
            .defaultValues("clear")
            .build();

    private final Map<String, ASTMethodCall> mdcKeysInUse = new HashMap<>();

    private final List<String> mdcClasses;
    private final List<String> putMethods;
    private final List<String> removeMethods;
    private final List<String> clearMethods;

    public PotentiallyThreadLocalPollutionByMdc() {
        definePropertyDescriptor(CLASSES);
        definePropertyDescriptor(PUT_METHODS);
        definePropertyDescriptor(REMOVE_METHODS);
        definePropertyDescriptor(CLEAR_METHODS);
        this.mdcClasses = getProperty(CLASSES);
        this.putMethods = getProperty(PUT_METHODS);
        this.removeMethods = getProperty(REMOVE_METHODS);
        this.clearMethods = getProperty(CLEAR_METHODS);
    }

    @Override
    public Object visit(final ASTMethodCall node, final Object data) {
        if (this.isMdc(node.getQualifier())) {
            final String key = extractKey(node);
            if (key != null && this.putMethods.contains(node.getMethodName())) {
                mdcKeysInUse.put(key, node);
            }
            if (key != null && this.removeMethods.contains(node.getMethodName())) {
                mdcKeysInUse.remove(key);
            }
            if (this.clearMethods.contains(node.getMethodName())) {
                mdcKeysInUse.clear();
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(final ASTMethodDeclaration node, final Object data) {
        node.children().forEach(child -> child.acceptVisitor(this, data));
        if (!this.mdcKeysInUse.isEmpty()) {
            for (final Map.Entry<String, ASTMethodCall> inv : this.mdcKeysInUse.entrySet()) {
                asCtx(data).addViolation(inv.getValue(), inv.getKey());
            }
        }
        this.mdcKeysInUse.clear();
        return super.visit(node, data);
    }

    private boolean isMdc(final ASTExpression qualifier) {
        if (qualifier == null) {
            return false;
        }
        return this.mdcClasses.stream()
                   .anyMatch(mdcClass -> TypeTestUtil.isA(mdcClass, qualifier.getTypeMirror()));
    }

    /**
     * Extract the key from the {@code MDC.put(...)} or {@code MDC.remove(...)} method call.
     * Assuming key is the first argument.
     */
    private String extractKey(final ASTMethodCall node) {
        if (node.getArguments().isEmpty()) {
            return null;
        }
        return node.getArguments().get(0).getImage();
    }
}
