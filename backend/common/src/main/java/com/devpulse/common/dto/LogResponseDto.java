package com.devpulse.common.dto;

import com.devpulse.common.enums.LogLevel;
import lombok.*;
import java.time.Instant;

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
}