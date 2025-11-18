package com.devpulse.alertprocessor.kafka;

import com.devpulse.common.constants.KafkaTopics;
import com.devpulse.common.dto.AlertMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes AlertMessageDto to the ALERT_TOPIC.
 * Kept small so it can be reused by other services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAlert(AlertMessageDto dto) {
        log.info("Publishing alert to {}: {}", KafkaTopics.ALERT_TOPIC, dto);
        kafkaTemplate.send(KafkaTopics.ALERT_TOPIC, dto);
    }
}
