package com.devpulse.logdashboard.model;

import com.devpulse.common.enums.LogLevel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for collected logs. Matches log-collector's persisted schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "logs")
public class LogDocument {

    @Id
    private String id;

    private String serviceName;
    private LogLevel level;
    private String message;
    private Instant timestamp;
    private String traceId;
}
