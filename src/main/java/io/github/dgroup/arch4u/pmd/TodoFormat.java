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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Ensure that TODO comment written in java comments are following the expected format.
 * The format described here: https://github.com/marketplace/actions/todo-to-issue
 * @since 0.2.0
 * @checkstyle ReturnCountCheck (400 lines)
 * @checkstyle IllegalTokenCheck (400 lines)
 * @checkstyle NonStaticMethodCheck (400 lines)
 * @checkstyle AvoidInlineConditionalsCheck (400 lines)
 * @checkstyle StringLiteralsConcatenationCheck (400 lines)
 */
@SuppressWarnings({
    "PMD.OnlyOneReturn",
    "PMD.AvoidDuplicateLiterals",
    "PMD.StaticAccessToStaticFields"})
public final class TodoFormat extends AbstractJavaRule {

    /**
     * Leading spaces count in lines under TODO marker.
     */
    public static final PropertyDescriptor<Integer> IDENTS =
        PropertyFactory.intProperty("indents")
            .defaultValue(1)
            .desc("Leading spaces count in lines under TODO marker")
            .build();

    /**
     * Symbols limit in issue subject.
     * Default GitHub limit for issue subject is 255.
     * @checkstyle MagicNumberCheck (5 lines)
     */
    public static final PropertyDescriptor<Integer> MAX_SUBJ =
        PropertyFactory.intProperty("maxSubjectSymbols")
            .defaultValue(255)
            .desc("Symbols limit in issue subject")
            .build();

    /**
     * The number of words to be mandatory within the description.
     */
    public static final PropertyDescriptor<Integer> MIN_WORDS =
        PropertyFactory.intProperty("minWords")
            .defaultValue(0)
            .desc("The number of words to be mandatory within the description")
            .build();

    /**
     * Flag to verify the existence of 'labels' section within the todo comment.
     */
    public static final PropertyDescriptor<Boolean> LABELS =
        PropertyFactory.booleanProperty("labels")
            .defaultValue(false)
            .desc("Flag to verify the existence of 'labels' section within the todo comment")
            .build();

    /**
     * Flag to verify the existence of 'assignees' section within the todo comment.
     */
    public static final PropertyDescriptor<Boolean> ASSIGNEES =
        PropertyFactory.booleanProperty("assignees")
            .defaultValue(false)
            .desc("Flag to verify the existence of 'assignees' section within the todo comment")
            .build();

    /**
     * Flag to verify the existence of 'milestone' section within the todo comment.
     */
    public static final PropertyDescriptor<Boolean> MILESTONE =
        PropertyFactory.booleanProperty("milestone")
            .defaultValue(false)
            .desc("Flag to verify the existence of 'milestone' section within the todo comment")
            .build();

    /**
     * Prohibited keywords that may have same semantic as todo.
     */
    public static final PropertyDescriptor<List<String>> PROHIBITED =
        PropertyFactory.stringListProperty("prohibited")
            .defaultValue(Collections.singletonList("fixme"))
            .desc("Prohibited keywords that may have same semantic as todo.")
            .build();

    /**
     * Ctor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public TodoFormat() {
        definePropertyDescriptor(IDENTS);
        definePropertyDescriptor(MAX_SUBJ);
        definePropertyDescriptor(MIN_WORDS);
        definePropertyDescriptor(LABELS);
        definePropertyDescriptor(ASSIGNEES);
        definePropertyDescriptor(MILESTONE);
        definePropertyDescriptor(PROHIBITED);
    }

    @Override
    public Object visit(final ASTCompilationUnit unit, final Object data) {
        for (final Todo todo : this.comments(unit)) {
            if (this.hasInvalidSubject(todo)) {
                asCtx(data).addViolationWithPosition(
                    unit, todo.subjectLineId, todo.lastLineId,
                    "The TODO subject \"{0}\" should start from 'TODO', non-blank and less or "
                        + "equal {1}: {2}",
                    todo.subject, this.getProperty(MAX_SUBJ), this.getExternalInfoUrl()
                );
            }
            if (this.hasInvalidDescription(todo)) {
                asCtx(data).addViolationWithPosition(
                    unit, todo.descriptionLineId, todo.lastLineId,
                    "The TODO description \"{0}\" should be aligned and has at least {1} or more "
                        + "words: {2}",
                    todo.description, this.getProperty(MIN_WORDS), this.getExternalInfoUrl()
                );
            }
            if (this.hasInvalid(LABELS, todo.labels, todo.index)) {
                asCtx(data).addViolationWithPosition(
                    unit, todo.labelsLineId, todo.lastLineId,
                    "The TODO label(s) \"{0}\" count should has at least {1} or more labels: {2}",
                    todo.labelsLineId, this.getProperty(LABELS), this.getExternalInfoUrl()
                );
            }
            if (this.hasInvalid(ASSIGNEES, todo.assignees, todo.index)) {
                asCtx(data).addViolationWithPosition(
                    unit, todo.assigneeLineId, todo.lastLineId,
                    "The TODO assignee(s) \"{0}\" count should be {1} or more labels: {2}",
                    todo.assigneeLineId, this.getProperty(ASSIGNEES), this.getExternalInfoUrl()
                );
            }
            if (this.hasInvalidMilestone(todo)) {
                asCtx(data).addViolationWithPosition(
                    unit, todo.milestoneLineId, todo.lastLineId,
                    "The TODO milestone id \"{0}\" is missing or invalid: {1}",
                    todo.milestone, this.getExternalInfoUrl()
                );
            }
        }
        return super.visit(unit, data);
    }

    /**
     * Find all java comments within the source file and transform them to parsed {@link Todo}.
     * It also able to parse multiple todo's within the single comment.
     * @param unit The java source file (e.g. compilation unit)
     * @return The collection of parsed todo items.
     */
    private Collection<Todo> comments(final ASTCompilationUnit unit) {
        return unit.getComments()
            .stream()
            .map(Todo::new)
            .peek(Todo::parse)
            .filter(Todo::present)
            .flatMap(Todo::multiple)
            .collect(Collectors.toList());
    }

    /**
     * Verify the subject for todo item.
     * @param todo The item to inspect.
     * @return The true if subject is empty, starting not from todo or has less words then expected.
     */
    private boolean hasInvalidSubject(final Todo todo) {
        return !StringUtils.startsWithIgnoreCase(todo.subject.trim(), "todo")
            || todo.subject.isEmpty()
            || todo.subject.length() > this.getProperty(MAX_SUBJ);
    }

    /**
     * Verify the description for todo item.
     * @param todo The item to inspect.
     * @return The true if description is empty but expected, has fewer words then expected, etc.
     * @checkstyle ReturnCountCheck (30 lines)
     */
    private boolean hasInvalidDescription(final Todo todo) {
        if (todo.description.isEmpty()) {
            return false;
        }
        final Set<Integer> indexes = todo.description.stream()
            .map(this::findFirstCharIndex)
            .collect(Collectors.toSet());
        if (indexes.size() > 1) {
            return true;
        }
        final int words = (int) todo.description.stream()
            .flatMap(line -> Stream.of(line.split("[[ ]*|[,]*|[\\.]*|[:]*|[/]*|[!]*|[?]*|[+]*]+")))
            .filter(StringUtils::isNotBlank)
            .count();
        return !this.sameIndent(indexes.iterator().next(), todo.index)
            || words < this.getProperty(MIN_WORDS);
    }

    /**
     * Verify the milestone for todo item.
     * @param todo The item to inspect.
     * @return The true if milestone is empty but expected or not a number.
     */
    private boolean hasInvalidMilestone(final Todo todo) {
        final String content = StringUtils.substringAfter(todo.milestone, ":").trim();
        final boolean digit = StringUtils.isNotBlank(content) && NumberUtils.isDigits(content);
        return this.getProperty(MILESTONE)
            ? !digit || this.hasInvalid(MILESTONE, todo.milestone, todo.index)
            : this.hasInvalid(MILESTONE, todo.milestone, todo.index);
    }

    /**
     * Ensure that expected tag is missing or has wrong indentation.
     * @param property The particular tag defined in PMD rule.
     * @param content The tag content
     * @param index The index of first char after T.O.D.O. keyword.
     * @return The true if tag has wrong structure or missing.
     */
    @SuppressWarnings("PMD.UselessParentheses")
    private boolean hasInvalid(final PropertyDescriptor<Boolean> property, final String content,
        final int index) {
        final boolean present = Stream.of(StringUtils.substringAfter(content, ":").split(","))
            .map(String::trim)
            .anyMatch(StringUtils::isNotBlank);
        return (!present && this.getProperty(property))
            || (present && !this.sameIndent(this.findFirstCharIndex(content), index));
    }

    /**
     * Find first index of non-space character.
     * @param line The tag from t.o.d.o. description.
     * @return The index.
     */
    private Integer findFirstCharIndex(final String line) {
        final char[] src = line.toCharArray();
        for (int index = 0; index < src.length; index++) {
            if (!Character.isWhitespace(src[index])) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Compare the indents between t.o.d.o. elements to be sure that they have expected indent.
     * @param actual The actual indent of particular t.o.d.o. element like labels, assignees, etc.
     * @param expected The expected indent with subject index {@link #findFirstCharIndex(String)}.
     * @return The true if indexes are correct considering {@link #IDENTS} rule property.
     */
    private boolean sameIndent(final int actual, final int expected) {
        return actual - this.getProperty(IDENTS) == expected;
    }

    /**
     * The object that represent a todo item(s) within one java comment.
     * @since 0.2.0
     * @checkstyle MemberNameCheck (200 lines)
     * @checkstyle MethodsOrderCheck (200 lines)
     * @checkstyle JavadocVariableCheck (200 lines)
     * @checkstyle VisibilityModifierCheck (200 lines)
     * @checkstyle MethodBodyCommentsCheck (200 lines)
     * @checkstyle EmptyLineSeparatorCheck (200 lines)
     * @checkstyle AvoidInlineConditionalsCheck (200 lines)
     */
    @SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods", "PMD.OnlyOneReturn"})
    public static final class Todo {

        public final List<Integer> found;
        public final List<Todo> todos;
        public final int beginLineId;
        public String subject;
        public List<String> description;
        public String assignees;
        public String labels;
        public String milestone;

        public int index;
        public int lastLineId;
        public int subjectLineId;
        public int descriptionLineId;
        public int labelsLineId;
        public int assigneeLineId;
        public int milestoneLineId;
        public String comment;

        /**
         * Ctor.
         * @param src The original java comment.
         */
        public Todo(final Comment src) {
            this(src.getImage(), src.getBeginLine());
        }

        /**
         * Ctor.
         * @param src The original java comment.
         * @param first The comment line number from source file begin.
         * @checkstyle ConditionalRegexpMultilineCheck (25 lines)
         */
        public Todo(final String src, final int first) {
            this.comment = src;
            this.found = new ArrayList<>(1);
            this.todos = new ArrayList<>(1);
            this.index = 0;
            this.description = new ArrayList<>(1);
            this.lastLineId = 0;
            this.labelsLineId = 0;
            this.beginLineId = first;
            this.assigneeLineId = 0;
            this.descriptionLineId = 0;
            this.labels = "";
            this.assignees = "";
            this.milestone = "";
        }

        /**
         * Parse each comment.
         * @return The 'this' to support fluent interface design.
         */
        @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
        public Todo parse() {
            final String[] lines = this.removeCommentPrefix(
                this.comment.split(System.lineSeparator())
            );
            for (int lid = 0; lid < lines.length; lid++) {
                if (lines[lid].toLowerCase().matches(".*\\stodo\\s*.*")) {
                    this.found.add(lid);
                }
            }
            if (!this.present()) {
                return this;
            }
            if (this.found.size() == 1) {
                this.parse(lines, this.found.iterator().next(), lines.length);
            } else {
                for (int idx = 0; idx < this.found.size(); idx++) {
                    final int next = idx + 1;
                    if (next < this.found.size()) {
                        this.parse(lines, this.found.get(idx), this.found.get(next));
                    } else {
                        final int line = this.found.get(idx);
                        this.parse(lines, line, line);
                    }
                }
            }
            return this;
        }

        /**
         * Ensure that java comment has no todo item defined.
         * @return True if no todo found.
         */
        public boolean present() {
            return !this.found.isEmpty();
        }

        /**
         * Parse the particular comment and split it to several todo's if they are defined.
         * @return All todo's defined within one java comment.
         * @todo #113/DEV So far multiple todo statements are not supported
         */
        public Stream<Todo> multiple() {
            return this.found.isEmpty() ? Stream.empty() : this.todos.stream();
        }

        @Override
        public String toString() {
            return this.comment;
        }

        /**
         * Parse single todo item within the java comment.
         * @param lines The java comment split by line separator.
         * @param subj The line number where subject is present.
         * @param next The line number for next t.o.d.o. item if its present.
         * @checkstyle ExecutableStatementCountCheck (50 lines)
         */
        private void parse(final String[] lines, final int subj, final int next) {
            final Todo todo = new Todo(this.comment, subj);
            // Find details for subject
            todo.lastLineId = next == 0 ? next : next - 1;
            todo.subject = lines[subj];
            todo.subjectLineId = subj;
            todo.index = StringUtils.indexOfIgnoreCase(todo.subject, "todo");
            todo.labelsLineId = subj;
            todo.assigneeLineId = subj;
            todo.milestoneLineId = subj;
            // Find lines for tags: 'labels', 'assignee', 'milestone'
            for (int line = subj; line < todo.lastLineId; line++) {
                if (StringUtils.isBlank(lines[line])) {
                    continue;
                }
                if (lines[line].matches("\\s*labels\\s*:\\s*.*")) {
                    todo.labelsLineId = line;
                    todo.labels = lines[line];
                }
                if (lines[line].matches("\\s*assignees\\s*:\\s*.*")) {
                    todo.assigneeLineId = line;
                    todo.assignees = lines[line];
                }
                if (lines[line].matches("\\s*milestone\\s*:\\s*.*")) {
                    todo.milestoneLineId = line;
                    todo.milestone = lines[line];
                }
            }
            // Find lines for tag 'description'
            final int desc = Stream.of(todo.labelsLineId, todo.assigneeLineId, todo.milestoneLineId)
                .filter(val -> val > 0)
                .min(Comparator.naturalOrder())
                .orElse(0) - 1;
            if (desc > 0 && desc != subj) {
                todo.descriptionLineId = todo.subjectLineId + 1;
                todo.description = Arrays.stream(lines, todo.descriptionLineId, desc + 1)
                    .collect(Collectors.toList());
            }
            // Setup line ids considering 1st comment line since source file
            todo.subjectLineId += this.beginLineId;
            todo.descriptionLineId += this.beginLineId;
            todo.assigneeLineId += this.beginLineId;
            todo.labelsLineId += this.beginLineId;
            todo.milestoneLineId += this.beginLineId;
            // t.o.d.o. is assembled, adding to the full list of processed items
            this.todos.add(todo);
        }

        /**
         * Remove the comment prefix from each line of comment.
         * @param lines The source lines.
         * @return The lines as is without javadoc prefix '*' or '//' symbol.
         * @checkstyle IllegalTokenCheck (50 lines)
         * @checkstyle TrailingCommentCheck (50 lines)
         * @checkstyle StringLiteralsConcatenationCheck (50 lines)
         */
        @SuppressWarnings("PMD.CyclomaticComplexity")
        private String[] removeCommentPrefix(final String... lines) {
            final String body = this.comment.trim();
            final boolean javadoc = body.startsWith("/**" + System.lineSeparator());
            final boolean multiline = body.startsWith("/*");
            final boolean oneline = body.startsWith("//");
            final String[] clean = new String[lines.length];
            for (int idx = 0; idx < lines.length; idx++) {
                if (javadoc && idx == 0) {                      // first line in javadoc
                    clean[idx] = "";
                    continue;
                } else if (StringUtils.isBlank(lines[idx])) {   // empty line in comment
                    clean[idx] = "";
                    continue;
                } else {                                        // non-empty line in comment
                    clean[idx] = lines[idx];
                }
                // Remove leading/trailing spaces for 1 line in comment
                final String trimmed = clean[idx].trim();
                // Remove closing tag for javadoc
                if (trimmed.endsWith("*/")) {
                    clean[idx] = clean[idx].substring(0, clean[idx].length() - 2);
                }
                // Remove leading star for javadoc or multiline comment
                if (javadoc || multiline && trimmed.charAt(0) == '*') {
                    clean[idx] = StringUtils.substringAfter(clean[idx], "*");
                }
                // Remove prefix if its single-line comment
                if (oneline) {
                    clean[idx] = StringUtils.substringAfter(clean[idx], "//");
                }
            }
            return clean;
        }
    }
}
