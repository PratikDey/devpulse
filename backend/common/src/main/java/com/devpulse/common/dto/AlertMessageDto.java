package com.devpulse.common.dto;

import com.devpulse.common.enums.AlertSeverity;
import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertMessageDto {

    private String serviceName;
    private AlertSeverity severity;
    private String message;
    private Instant timestamp;

    private String traceId;  // log-based alerts
    private String ruleId;   // rule that triggered
    private String details;  // explanation or stack

    // NEW: identify source (LOG / METRIC / PROMETHEUS)
    private String sourceType;

    // NEW: extra contextual data
    private Map<String, Object> context;

}
