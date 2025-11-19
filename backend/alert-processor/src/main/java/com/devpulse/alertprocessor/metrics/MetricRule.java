package com.devpulse.alertprocessor.metrics;

import com.devpulse.common.enums.AlertSeverity;
import lombok.Data;

/**
 * Represents a single alert rule loaded from alert-rules.yml.
 *
 * Each rule defines:
 *  - promql query to run
 *  - threshold comparison
 *  - duration over which metric must breach threshold
 *  - severity & description
 */
@Data
public class MetricRule {

    /** Unique rule ID (used in AlertDocument.ruleId) */
    private String id;

    /** Human-friendly rule name */
    private String name;

    /** PromQL query string executed against Prometheus */
    private String promql;

    /** Numeric threshold value */
    private double threshold;

    /**
     * GREATER_THAN | LESS_THAN | GREATER_OR_EQUAL | LESS_OR_EQUAL
     * Defines how metric value is compared to threshold.
     */
    private String comparator;

    /** Duration for which the condition must be true before triggering */
    private int durationSeconds;

    /** Severity of the alert (INFO, WARNING, CRITICAL) */
    private AlertSeverity severity;

    /** Description shown in UI / API responses */
    private String description;
}