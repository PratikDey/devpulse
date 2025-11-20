package com.devpulse.alertprocessor.metrics;

import org.springframework.stereotype.Component;

@Component
public class MetricAlertEvaluator {

    /**
     * Evaluate a numeric metric value against a {@link MetricRule}.
     *
     * Supported comparator string values:
     *  - "GREATER_THAN"
     *  - "GREATER_OR_EQUAL"
     *  - "LESS_THAN"
     *  - "LESS_OR_EQUAL"
     *
     * Returns true when the provided value satisfies the rule's comparator/threshold.
     *
     * @param rule  metric rule containing comparator and threshold
     * @param value observed numeric value to evaluate
     * @return true if the rule condition is met
     */

    public boolean evaluate(MetricRule rule, double value) {
        return switch (rule.getComparator()) {
            case "GREATER_THAN" -> value > rule.getThreshold();
            case "GREATER_OR_EQUAL" -> value >= rule.getThreshold();
            case "LESS_THAN" -> value < rule.getThreshold();
            case "LESS_OR_EQUAL" -> value <= rule.getThreshold();
            default -> false;
        };
    }
}
