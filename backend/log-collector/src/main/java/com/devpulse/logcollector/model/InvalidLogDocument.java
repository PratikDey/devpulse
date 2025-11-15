package com.devpulse.logcollector.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for storing invalid log messages.
 * These messages failed JSON deserialization and require manual inspection.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "logs_errors")
public class InvalidLogDocument {

    @Id
    private String id;

    /** The raw log string exactly as it was received from Kafka */
    private String rawMessage;

    /** Kafka metadata to help debug message origin */
    private String topic;
    private int partition;
    private long offset;

    /** Timestamp when the log was processed */
    private Instant timestamp;
}
