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

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test case for {@link TodoFormat.Todo}.
 *
 * @since 0.2.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle AvoidStaticImportCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TodoTest {

    @Test
    public void hasSingleTodo() {
        Assertions.assertThat(
            new TodoFormat.Todo(this.singleTodoBody(), 0)
                .parse()
                .present()
        ).as("One todo is found").isTrue();
    }

    @Test
    public void hasProperSubject() {
        Assertions.assertThat(
            new TodoFormat.Todo(this.singleTodoBody(), 0)
                .parse()
                .multiple()
                .findFirst()
                .get()
                .subject
        ).as("Single todo has proper subject").isEqualTo(" TODO Just do it");
    }

    @Test
    public void hasProperDescription() {
        Assertions.assertThat(
            new TodoFormat.Todo(this.singleTodoBody(), 0)
                .parse()
                .multiple()
                .findFirst()
                .get()
                .description
        ).as("Single todo has proper description").containsExactly(
            "  Simple multiline description line 1",
            "  Simple multiline description line 2"
        );
    }

    @Test
    public void hasProperAssignees() {
        Assertions.assertThat(
            new TodoFormat.Todo(this.singleTodoBody(), 0)
                .parse()
                .multiple()
                .findFirst()
                .get()
                .assignees
        ).as("Single todo has proper assignees").isEqualTo("  assignees: dgroup, someoneelse");
    }

    @Test
    public void hasProperLabels() {
        Assertions.assertThat(
            new TodoFormat.Todo(this.singleTodoBody(), 0)
                .parse()
                .multiple()
                .findFirst()
                .get()
                .assignees
        ).as("Single todo has proper labels").isEqualTo("  assignees: dgroup, someoneelse");
    }

    @Test
    public void hasProperMilestone() {
        Assertions.assertThat(
            new TodoFormat.Todo(this.singleTodoBody(), 0)
                .parse()
                .multiple()
                .findFirst()
                .get()
                .milestone
        ).as("Single todo has proper milestone").isEqualTo("  milestone: 2");
    }

    /**
     * The javadoc example with single T.O.D.O. statement.
     * @return Comment body.
     */
    private String singleTodoBody() {
        return new StringBuilder()
            .append("/**").append(System.lineSeparator())
            .append(" * TODO Just do it").append(System.lineSeparator())
            .append(" *  Simple multiline description line 1").append(System.lineSeparator())
            .append(" *  Simple multiline description line 2").append(System.lineSeparator())
            .append(" *  assignees: dgroup, someoneelse").append(System.lineSeparator())
            .append(" *  labels: wontfix, tbd").append(System.lineSeparator())
            .append(" *  milestone: 2").append(System.lineSeparator())
            .append(" */")
            .toString();
    }
}
