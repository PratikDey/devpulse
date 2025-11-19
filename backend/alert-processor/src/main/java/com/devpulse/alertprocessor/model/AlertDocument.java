package com.devpulse.alertprocessor.model;

import com.devpulse.common.enums.AlertSeverity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * MongoDB document storing alerts (both log-based and metric-based).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "alerts")
public class AlertDocument {

    @Id
    private String id;

    private String serviceName;
    private AlertSeverity severity;
    private String message;
    private Instant timestamp;

    // Optional common fields
    private String traceId;
    private String ruleId;
    private String details;

    // Flexible context to store extra information (metric value, labels, counts)
    private Map<String, Object> context;
}