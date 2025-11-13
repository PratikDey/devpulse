package com.devpulse.common.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricDataDto {
    private String metricName;
    private double value;
    private String unit;
    private Instant timestamp;
    private String serviceName;
}