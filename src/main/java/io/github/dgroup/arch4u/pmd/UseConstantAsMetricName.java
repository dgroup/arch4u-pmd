package io.github.dgroup.arch4u.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMemberValuePair;
import net.sourceforge.pmd.lang.java.ast.ASTStringLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;

public class UseConstantAsMetricName extends AbstractJavaRule {

    private static final PropertyDescriptor<List<String>> METRIC_ANNOTATIONS_DESCRIPTOR =
            PropertyFactory.stringListProperty("metricAnnotations")
                    .desc("List of the metric annotations.")
                    .defaultValues("io.micrometer.core.annotation.Timed")
                    .build();

    private final List<String> metricAnnotations;

    public UseConstantAsMetricName() {
        definePropertyDescriptor(METRIC_ANNOTATIONS_DESCRIPTOR);
        metricAnnotations = getProperty(METRIC_ANNOTATIONS_DESCRIPTOR);
    }

    @Override
    public Object visit(ASTAnnotation annotation, Object data) {
        if (isMetricAnnotation(annotation)) {
            annotation.getMembers()
                    .map(ASTMemberValuePair::getValue)
                    .filter(val -> val instanceof ASTStringLiteral)
                    .forEach(val -> asCtx(data).addViolation(val));
        }
        return super.visit(annotation, data);
    }

    private boolean isMetricAnnotation(ASTAnnotation node) {
        return metricAnnotations.stream().anyMatch(annot -> TypeTestUtil.isExactlyA(annot, node));
    }
}
