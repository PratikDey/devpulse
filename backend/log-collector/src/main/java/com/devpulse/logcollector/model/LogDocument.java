package com.devpulse.logcollector.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "logs")
public class LogDocument {

    @Id
    private String id;

    private String serviceName;
    private String message;
    private String level;
    private Instant timestamp;
}