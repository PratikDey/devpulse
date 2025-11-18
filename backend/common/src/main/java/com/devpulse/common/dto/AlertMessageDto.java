package com.devpulse.common.dto;

import com.devpulse.common.enums.AlertSeverity;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertMessageDto {
    private String serviceName;
    private AlertSeverity severity;
    private String message;
    private Instant timestamp;
    private String traceId;
    private String ruleId;
    private String details;
}
