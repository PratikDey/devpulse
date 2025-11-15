package com.devpulse.logcollector.service;

import com.devpulse.logcollector.model.InvalidLogDocument;
import com.devpulse.logcollector.repository.InvalidLogRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;



/**
 * InvalidLogService
 *
 * Stores invalid, non-JSON, or corrupted messages separately.
 * This allows developers to debug incorrect data sent by producers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvalidLogService {

    // Doesn't require @Autowired because constructor injection happening using Lombok's @RequiredArgsConstructor
    private final InvalidLogRepository repository;

    /**
     * Saves an invalid log entry into the "logs_errors" collection.
     *
     * @param topic       Kafka topic name
     * @param partition   Kafka partition number
     * @param offset      Kafka record offset
     * @param rawMessage  The message string as received (invalid JSON)
     */
    public void saveInvalidLog(String topic, int partition, long offset, String rawMessage) {

        InvalidLogDocument doc = InvalidLogDocument.builder()
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .rawMessage(rawMessage)
                .timestamp(Instant.now())
                .build();

        repository.save(doc);

        log.warn("Invalid log persisted to logs_errors (topic={}, partition={}, offset={})",
                topic, partition, offset);
    }
}
