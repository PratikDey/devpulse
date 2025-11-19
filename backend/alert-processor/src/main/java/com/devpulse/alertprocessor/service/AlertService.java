package com.devpulse.alertprocessor.service;

import com.devpulse.common.dto.AlertMessageDto;
import com.devpulse.alertprocessor.model.AlertDocument;
import com.devpulse.alertprocessor.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

/**
 * Central alert handler:
 *  - persists alerts
 *  - broadcasts via WebSocket
 *  - triggers email notifications (non-blocking)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;
    private final WebSocketAlertPublisher wsPublisher;
    private final EmailAlertService emailService;

    /**
     * Central alert processing routine.
     *
     * Responsibilities:
     *  - Normalize incoming DTO timestamp (fall back to Instant.now()).
     *  - Convert DTO to {@link com.devpulse.alertprocessor.model.AlertDocument} and persist via {@link com.devpulse.alertprocessor.repository.AlertRepository}.
     *  - Broadcast the saved document to WebSocket subscribers.
     *  - Trigger email notifications for non-INFO severities (best-effort; failures are logged).
     *
     * Notes:
     *  - Broadcasting and email sending are executed in try/catch blocks to avoid failing the persistence path.
     *  - Consider making email sending asynchronous and making recipient configuration external for production use.
     *
     * @param dto incoming alert DTO
     */

    public void handleAlert(AlertMessageDto dto) {
        // normalize timestamp
        Instant ts = dto.getTimestamp() == null ? Instant.now() : dto.getTimestamp();

        AlertDocument doc = AlertDocument.builder()
                .serviceName(dto.getServiceName())
                .severity(dto.getSeverity())
                .message(dto.getMessage())
                .timestamp(ts)
                .traceId(dto.getTraceId())
                .ruleId(dto.getRuleId())
                .details(dto.getDetails())
                .context(dto.getContext() == null ? new HashMap<>() : dto.getContext())
                .build();

        AlertDocument saved = repository.save(doc);
        log.info("Saved alert {} rule={}", saved.getId(), saved.getRuleId());

        // broadcast (non-blocking)
        try {
            wsPublisher.broadcastAlert(saved);
        } catch (Exception ex) {
            log.error("WebSocket broadcast failed", ex);
        }

        // email (best-effort)
        try {
            if (dto.getSeverity() != null && !"INFO".equalsIgnoreCase(dto.getSeverity().name())) {
                emailService.sendAlertEmail(dto);
            }
        } catch (Exception ex) {
            log.error("Failed to send alert email", ex);
        }
    }
}
