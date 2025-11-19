package com.devpulse.logcollector.alert;

import com.devpulse.common.constants.KafkaTopics;
import com.devpulse.common.dto.AlertMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes unified AlertMessageDto to Kafka topic defined in common.KafkaTopics.
 * Uses a KafkaTemplate<String, Object> so value is serialized as JSON.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish an alert to the central Kafka alert topic.
     */
    public void publish(AlertMessageDto dto) {
        try {
            kafkaTemplate.send(KafkaTopics.ALERT_TOPIC, dto.getServiceName(), dto);
            log.info("Published alert to topic {} : {}", KafkaTopics.ALERT_TOPIC, dto);
        } catch (Exception ex) {
            log.error("Failed to publish alert: {}", dto, ex);
        }
    }
}
