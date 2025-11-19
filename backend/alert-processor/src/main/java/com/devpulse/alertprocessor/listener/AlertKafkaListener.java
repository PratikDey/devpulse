package com.devpulse.alertprocessor.listener;

import com.devpulse.common.constants.KafkaTopics;
import com.devpulse.common.dto.AlertMessageDto;
import com.devpulse.alertprocessor.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes alert events produced by log-collector (topic: devpulse-alerts).
 * We receive message as raw JSON string and convert to AlertMessageDto using ObjectMapper.
 * This avoids custom ConsumerFactory and keeps config YAML-only.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertKafkaListener {

    private final AlertService alertService;
    private final ObjectMapper objectMapper;

    /**
     * Kafka message handler for raw JSON alert messages produced by log-collector.
     *
     * The method expects the Kafka record value to be a JSON representation of {@link com.devpulse.common.dto.AlertMessageDto}.
     * Deserialization errors are caught and logged; raw payloads are preserved in the log for later inspection.
     *
     * @param record Kafka consumer record containing a String JSON payload
     */

    @KafkaListener(topics = KafkaTopics.ALERT_TOPIC, groupId = "alert-processor-group")
    public void onMessage(ConsumerRecord<String, String> record) {
        String payload = record.value();
        try {
            AlertMessageDto dto = objectMapper.readValue(payload, AlertMessageDto.class);
            log.info("Consumed alert from Kafka (topic={}, partition={}, offset={}): {}",
                    record.topic(), record.partition(), record.offset(), dto);
            alertService.handleAlert(dto);
        } catch (Exception ex) {
            log.error("Failed to deserialize alert JSON from Kafka: {} - raw: {}", ex.getMessage(), payload, ex);
            // Optionally persist raw payload to "alerts_errors" collection — not implemented here
        }
    }
}
