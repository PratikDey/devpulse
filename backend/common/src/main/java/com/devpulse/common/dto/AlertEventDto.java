package com.devpulse.common.dto;

import com.devpulse.common.enums.AlertSeverity;
import lombok.*;
import java.time.Instant;

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
}