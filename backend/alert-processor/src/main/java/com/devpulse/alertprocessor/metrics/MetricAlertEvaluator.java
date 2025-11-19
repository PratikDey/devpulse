package com.devpulse.alertprocessor.metrics;

import org.springframework.stereotype.Component;

@Component
public class MetricAlertEvaluator {
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
