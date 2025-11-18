package com.devpulse.alertprocessor.service;

import com.devpulse.alertprocessor.model.AlertDocument;
import com.devpulse.alertprocessor.repository.AlertRepository;
import com.devpulse.common.constants.KafkaTopics;
import com.devpulse.common.dto.AlertMessageDto;
import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.common.enums.AlertSeverity;
import com.devpulse.common.enums.LogLevel;
import com.devpulse.alertprocessor.kafka.AlertProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AlertService: rule evaluation, persistence, and publishing.
 *
 * Note: This uses in-memory windows suitable for MVP/hackathon.
 * For production, replace with Redis/Kafka Streams/Time-series store.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertProducer alertProducer;
    private final ObjectMapper objectMapper;

    // Sliding windows per service for ERROR counts
    private final ConcurrentHashMap<String, Deque<Instant>> errorWindows = new ConcurrentHashMap<>();

    // Configurable thresholds (move to properties later)
    private final int errorWindowSeconds = 60;
    private final int errorThreshold = 5;

    private final List<String> criticalKeywords = List.of("OutOfMemory", "CRITICAL", "FATAL");

    /**
     * Called by listener when a log message is received (parsed to LogMessageDto).
     * Evaluates simple rules and triggers alerts when rules fire.
     */
    public void onLog(LogMessageDto dto) {
        if (dto == null) return;

        String service = dto.getServiceName() != null ? dto.getServiceName() : "unknown";
        String message = dto.getMessage() != null ? dto.getMessage() : "";

        // Immediate keyword-based critical alerts
        for (String kw : criticalKeywords) {
            if (message.contains(kw)) {
                emitAlert(dto, AlertSeverity.CRITICAL, "keyword-rule:" + kw);
                return;
            }
        }

        // Error spike rule
        if (dto.getLevel() == LogLevel.ERROR) {
            Deque<Instant> window = errorWindows.computeIfAbsent(service, k -> new ArrayDeque<>());
            synchronized (window) {
                Instant now = dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now();
                window.addLast(now);

                Instant cutoff = Instant.now().minusSeconds(errorWindowSeconds);
                while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
                    window.removeFirst();
                }

                if (window.size() >= errorThreshold) {
                    emitAlert(dto, AlertSeverity.WARNING, "error-spike");
                    window.clear(); // avoid repeated alerts for the same spike
                }
            }
        }
    }

    private void emitAlert(LogMessageDto dto, AlertSeverity severity, String ruleId) {
        AlertDocument doc = AlertDocument.builder()
                .serviceName(dto.getServiceName())
                .severity(severity)
                .message("Alert triggered: " + ruleId + " | " + dto.getMessage())
                .timestamp(Instant.now())
                .traceId(dto.getTraceId())
                .ruleId(ruleId)
                .details("originalTimestamp=" + dto.getTimestamp())
                .build();

        // persist
        alertRepository.save(doc);
        log.info("Alert persisted: {}", doc);

        // build message for Kafka
        AlertMessageDto alertDto = AlertMessageDto.builder()
                .serviceName(doc.getServiceName())
                .severity(doc.getSeverity())
                .message(doc.getMessage())
                .timestamp(doc.getTimestamp())
                .traceId(doc.getTraceId())
                .ruleId(doc.getRuleId())
                .details(doc.getDetails())
                .build();

        // publish Kafka
        alertProducer.sendAlert(alertDto);
    }

    // optional cleanup of windows every minute
    @Scheduled(fixedDelay = 60_000)
    public void cleanupWindows() {
        Instant cutoff = Instant.now().minusSeconds(errorWindowSeconds);
        for (Map.Entry<String, Deque<Instant>> e : errorWindows.entrySet()) {
            Deque<Instant> dq = e.getValue();
            synchronized (dq) {
                while (!dq.isEmpty() && dq.peekFirst().isBefore(cutoff)) {
                    dq.removeFirst();
                }
                if (dq.isEmpty()) {
                    errorWindows.remove(e.getKey(), dq);
                }
            }
        }
    }
}
