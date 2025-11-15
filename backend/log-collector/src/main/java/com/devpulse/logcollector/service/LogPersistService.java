package com.devpulse.logcollector.service;

import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.logcollector.model.LogDocument;
import com.devpulse.logcollector.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * LogPersistService
 *
 * Responsible for saving valid log entries into MongoDB.
 * Converts the DTO received from Kafka into a MongoDB document.
 */

@Service
@RequiredArgsConstructor
public class LogPersistService {

    // Doesn't require @Autowired because constructor injection happening using Lombok's @RequiredArgsConstructor
    private final LogRepository logRepository;

    /**
     * Saves a valid log message to MongoDB.
     *
     * @param dto the log message received from Kafka
     */
    public void saveLog(LogMessageDto dto) {
        LogDocument doc = LogDocument.builder()
                .serviceName(dto.getServiceName())
                .message(dto.getMessage())
                .level(dto.getLevel() != null ? dto.getLevel().name() : null)
                .timestamp(dto.getTimestamp())
                .traceId(dto.getTraceId())
                .build();

        logRepository.save(doc);
    }
}