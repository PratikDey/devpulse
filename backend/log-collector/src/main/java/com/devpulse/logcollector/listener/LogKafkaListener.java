package com.devpulse.logcollector.listener;

import com.devpulse.common.constants.KafkaTopics;
import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.logcollector.service.LogPersistService;
import com.devpulse.logcollector.service.InvalidLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * LogKafkaListener
 *
 * This class listens to the Kafka topic "devpulse-logs" and processes log
 * messages.
 * It supports two scenarios:
 *
 * 1. VALID LOG MESSAGE (Valid JSON → LogMessageDto):
 * - Persist into MongoDB (logs collection)
 *
 * 2. INVALID LOG MESSAGE (Invalid JSON, corrupted, wrong schema):
 * - We detect invalid payload and store it in a separate MongoDB collection
 * (logs_errors)
 *
 * This design ensures fault-tolerance and avoids infinite retry loops.
 *
 * We receive ALL Kafka messages as RAW STRING.
 * Reason:
 * - Prevent Jackson failures inside Kafka listener container.
 * - Full control over error-handling.
 * - Ability to store invalid logs separately.
 *
 * Responsibilities:
 * 1. Try parse string → LogMessageDto
 * 2. If parsing succeeds → save to logs collection
 * 3. If parsing fails → save raw log to logs_errors collection
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class LogKafkaListener {

    // Doesn't require @Autowired because constructor injection happening using
    // Lombok's @RequiredArgsConstructor
    private final LogPersistService persistService;
    private final InvalidLogService invalidLogService;
    private final com.devpulse.logcollector.service.DashboardPushService pushService;
    private final ObjectMapper objectMapper; // Spring Boot's mapper -> no manual injection

    /**
     * Receives raw messages from Kafka, attempts JSON parsing, and stores
     * valid vs invalid logs in separate MongoDB collections.
     *
     * @param rawMessage The raw message payload from Kafka (JSON or invalid text)
     * @param record     Metadata-rich Kafka record (topic, partition, offset)
     */
    @KafkaListener(topics = KafkaTopics.LOG_TOPIC, groupId = "log-collector-group")
    public void consume(String rawMessage, ConsumerRecord<String, String> record) {

        try {
            // Try converting raw JSON into our DTO
            LogMessageDto dto = objectMapper.readValue(rawMessage, LogMessageDto.class);

            // Valid → persist to "logs"
            log.info("Received VALID log from Kafka: {}", dto);
            persistService.saveLog(dto);

            // Push to dashboard for live streaming
            pushService.pushLog(dto);

        } catch (Exception ex) {

            // Invalid → persist raw + metadata
            log.error("INVALID log received: {}", rawMessage);

            invalidLogService.saveInvalidLog(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    rawMessage);
        }
    }
}
