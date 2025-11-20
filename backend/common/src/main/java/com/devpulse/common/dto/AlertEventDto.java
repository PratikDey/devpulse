package com.devpulse.common.dto;

import com.devpulse.common.enums.AlertSeverity;
import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEventDto {
    private String alertId;
    private String sourceService;
    private String description;
    private AlertSeverity severity;
    private Instant timestamp;

    // NEW fields
    private String traceId;
    private String ruleId;
    private String sourceType;
    private Map<String, Object> context;
}