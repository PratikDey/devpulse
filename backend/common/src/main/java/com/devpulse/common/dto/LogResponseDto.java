package com.devpulse.common.dto;

import com.devpulse.common.enums.LogLevel;
import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogResponseDto {
    private String id;
    private String serviceName;
    private LogLevel level;
    private String message;
    private Instant timestamp;

    private String traceId;
    private Map<String, Object> metadata;
    private String sourceType;
}