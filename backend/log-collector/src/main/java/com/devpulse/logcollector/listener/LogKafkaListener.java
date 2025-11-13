package com.devpulse.logcollector.listener;

import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.logcollector.service.LogPersistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogKafkaListener {

    private final LogPersistService persistService;

    @KafkaListener(topics = "devpulse-logs", groupId = "log-collector-group")
    public void consume(LogMessageDto logMessage) {
        log.info("Received log: {}", logMessage);
        try {
            persistService.saveLog(logMessage);
        } catch (Exception ex) {
            log.error("Failed to persist log: {}", logMessage, ex);
            // optionally send to DLQ or monitoring
        }
    }
}