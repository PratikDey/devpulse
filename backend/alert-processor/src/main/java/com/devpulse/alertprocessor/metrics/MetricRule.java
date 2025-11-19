package com.devpulse.alertprocessor.metrics;

import com.devpulse.common.enums.AlertSeverity;
import lombok.Data;

@Data
public class MetricRule {
    private String id;
    private String name;
    private String promql;
    private double threshold;
    private String comparator; // GREATER_THAN etc.
    private int durationSeconds;
    private AlertSeverity severity;
    private String description;
}
