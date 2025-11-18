package com.devpulse.alertprocessor.listener;

import com.devpulse.alertprocessor.service.AlertService;
import com.devpulse.common.dto.LogMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to raw log messages on Kafka topic (devpulse-logs),
 * parses JSON to LogMessageDto and forwards to AlertService.
 *
 * We use StringDeserializer at consumer config and parse manually to avoid container-level deserialization failures.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertKafkaListener {

    private final ObjectMapper objectMapper;
    private final AlertService alertService;

    @KafkaListener(topics = "devpulse-logs", groupId = "alert-processor-group")
    public void consume(String rawMessage, ConsumerRecord<String, String> record) {

        try {
            LogMessageDto dto = objectMapper.readValue(rawMessage, LogMessageDto.class);
            log.debug("AlertListener received log: {}", dto);
            alertService.onLog(dto);
        } catch (Exception ex) {
            log.warn("Failed to parse log in alert-processor: {} | raw: {}", ex.getMessage(), rawMessage);
            // Optionally persist invalid messages or forward to a DLQ
        }
    }
}
