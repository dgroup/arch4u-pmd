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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.xpath.TypeIsFunction;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * A rule that prohibits the using methods of a particular class.
 *
 * @see
 * <a href="https://github.com/dgroup/arch4u-pmd/issues/22">https://github.com/dgroup/arch4u-pmd/issues/22</a>
 * @since 0.1.0
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class ObfuscationRequired extends AbstractJavaRule {

    /**
     * Property descriptor with test class suffix.
     */
    private static final PropertyDescriptor<List<String>> LOGGERS =
        PropertyFactory.stringListProperty("loggerClasses")
            .desc("The fully qualified name of the class for logging")
            .defaultValues(
                "org.slf4j.Logger",
                "java.util.logging.Logger",
                "org.apache.log4j.Logger",
                "org.apache.logging.log4j.Logger"
            )
            .build();

    /**
     * Property descriptor with the list of the prohibited classes.
     */
    private static final PropertyDescriptor<List<String>> CLASSES =
        PropertyFactory.stringListProperty("sensitiveClasses")
            .desc("List of prohibited methods")
            .emptyDefaultValue()
            .build();

    /**
     * Property descriptor with the list of the prohibited packages.
     */
    private static final PropertyDescriptor<List<String>> PACKAGES =
        PropertyFactory.stringListProperty("sensitivePackages")
            .desc("List of prohibited packages")
            .emptyDefaultValue()
            .build();

    /**
     * Constructor for defining property descriptor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public ObfuscationRequired() {
        this.definePropertyDescriptor(LOGGERS);
        this.definePropertyDescriptor(CLASSES);
        this.definePropertyDescriptor(PACKAGES);
    }

    @Override
    public Object visit(final ASTVariableDeclaratorId vardecl, final Object data) {
        if (this.isLogger(vardecl.getTypeNode())) {
            for (final NameOccurrence usage : vardecl.getUsages()) {
                final JavaNameOccurrence occurrence = (JavaNameOccurrence) usage;
                getArguments(occurrence)
                    .stream()
                    .filter(this::hasSensitiveData)
                    .forEach(arg -> this.addViolation(data, arg));
            }
        }
        return data;
    }

    /**
     * Checks if the provided type is logger.
     * @param type Type node.
     * @return True if the type is logger.
     */
    private boolean isLogger(final ASTType type) {
        final boolean matches;
        if (type != null) {
            matches = false;
        } else {
            matches = this.getProperty(LOGGERS)
                .stream()
                .anyMatch(logger -> TypeIsFunction.typeIs(type, logger));
        }
        return matches;
    }

    /**
     * Gets argument of logger invocation.
     * @param occurrence Name occurrence.
     * @return List of arguments as expressions.
     */
    private static List<ASTExpression> getArguments(final JavaNameOccurrence occurrence) {
        return Optional.ofNullable(occurrence.getLocation())
            .map(loc -> loc.getFirstParentOfType(ASTPrimaryExpression.class))
            .map(prex -> prex.getFirstChildOfType(ASTPrimarySuffix.class))
            .map(suf -> suf.getFirstChildOfType(ASTArguments.class))
            .map(args -> args.getFirstChildOfType(ASTArgumentList.class))
            .map(arglist -> arglist.findChildrenOfType(ASTExpression.class))
            .orElse(Collections.emptyList());
    }

    /**
     * Checks if the argument is a class with sensitive data
     * or if it contains in the prohibited package with such classes.
     * @param argument Expression node, logger argument.
     * @return True if there is sensitive data.
     */
    private boolean hasSensitiveData(final ASTExpression argument) {
        return this.isSensitiveData(argument) || this.isInProhibitedPackage(argument);
    }

    /**
     * Checks if the object has sensitive data. In this case it's not allowed
     * to log it without applying obfuscation.
     * @param argument Expression node, logger argument.
     * @return True if the argument contains sensitive data.
     */
    private boolean isSensitiveData(final ASTExpression argument) {
        final Node node;
        if (hasDirectToStringInvocation(argument)) {
            node = argument.getFirstChildOfType(ASTPrimaryExpression.class)
                .getFirstChildOfType(ASTPrimaryPrefix.class);
        } else {
            node = argument;
        }
        return this.getProperty(CLASSES)
            .stream()
            .anyMatch(clss -> TypeIsFunction.typeIs(node, clss));
    }

    /**
     * Checks if the expression has direct {@code toString} invocation.
     * In this case, the type should be checked for PrimaryExpression/PrimaryPrefix node.
     * @param expression Expression node, logger argument.
     * @return True if there is {@code toString} invocation.
     */
    private static boolean hasDirectToStringInvocation(final ASTExpression expression) {
        return Optional.ofNullable(expression.getFirstChildOfType(ASTPrimaryExpression.class))
            .map(prex -> prex.getFirstChildOfType(ASTPrimaryPrefix.class))
            .map(prefix -> prefix.getFirstChildOfType(ASTName.class))
            .map(AbstractNode::getImage)
            .filter(img -> img.endsWith(".toString"))
            .isPresent();
    }

    /**
     * Checks if the argument contains in the prohibited package.
     * @param node Expression node, logger argument.
     * @return True if the argument contains in the prohibited package.
     */
    private boolean isInProhibitedPackage(final TypeNode node) {
        final String fulltypename = Optional.ofNullable(node.getType())
            .map(Class::getTypeName)
            .orElse(null);
        return fulltypename != null
            && this.getProperty(PACKAGES).stream().anyMatch(fulltypename::startsWith);
    }

}
