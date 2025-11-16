package com.devpulse.producerorder.kafka;

import com.devpulse.common.constants.KafkaTopics;
import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.common.enums.LogLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * LogProducer
 *
 * Responsible for creating and publishing structured LogMessageDto events
 * to the shared topic (KafkaTopics.LOG_TOPIC).
 *
 * Implementation notes:
 * - Uses Spring Boot auto-configured KafkaTemplate (JsonSerializer configured via YAML).
 * - Generates traceId here if caller does not provide one.
 * - Keeps messages simple and consistent with common/ LogMessageDto.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send a log event to Kafka.
     *
     * @param serviceName producer service name (e.g. "producer-order")
     * @param level       log level enum
     * @param message     human readable message
     * @param traceId     correlation id (if null, new UUID will be generated)
     */
    public void sendLog(String serviceName, LogLevel level, String message, String traceId) {
        String effectiveTrace = traceId != null ? traceId : UUID.randomUUID().toString();

        LogMessageDto dto = LogMessageDto.builder()
                .serviceName(serviceName)
                .level(level)
                .message(message)
                .timestamp(Instant.now())
                .traceId(effectiveTrace)
                .build();

        log.info("Publishing log to Kafka topic {}: {}", KafkaTopics.LOG_TOPIC, dto);
        kafkaTemplate.send(KafkaTopics.LOG_TOPIC, dto);
    }
}
