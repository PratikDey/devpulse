package com.devpulse.alertprocessor.model;

import com.devpulse.common.enums.AlertSeverity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Persisted alert record.
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
    private String traceId;
    private String ruleId;
    private String details;
}
