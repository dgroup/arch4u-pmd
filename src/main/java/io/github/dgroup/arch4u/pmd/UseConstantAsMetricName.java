package io.github.dgroup.arch4u.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMemberValuePair;
import net.sourceforge.pmd.lang.java.ast.ASTStringLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;

@SuppressWarnings({
    "PMD.StaticAccessToStaticFields",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public class UseConstantAsMetricName extends AbstractJavaRule {

    private static final PropertyDescriptor<List<String>> ANNOTATIONS =
        PropertyFactory.stringListProperty("metricAnnotations")
            .desc("List of the metric annotations.")
            .defaultValues("io.micrometer.core.annotation.Timed")
            .build();

    private final List<String> annotations;

    public UseConstantAsMetricName() {
        definePropertyDescriptor(ANNOTATIONS);
        this.annotations = getProperty(ANNOTATIONS);
    }

    @Override
    public Object visit(final ASTAnnotation annotation, final Object data) {
        if (isMetricAnnotation(annotation)) {
            annotation.getMembers()
                .map(ASTMemberValuePair::getValue)
                .filter(val -> val instanceof ASTStringLiteral)
                .forEach(val -> asCtx(data).addViolation(val));
        }
        return super.visit(annotation, data);
    }

    private boolean isMetricAnnotation(final ASTAnnotation node) {
        return this.annotations.stream()
                   .anyMatch(annot -> TypeTestUtil.isExactlyA(annot, node));
    }
}
