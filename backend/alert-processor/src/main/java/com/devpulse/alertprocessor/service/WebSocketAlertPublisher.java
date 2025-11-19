package com.devpulse.alertprocessor.service;

import com.devpulse.alertprocessor.model.AlertDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcast saved alerts to frontend via /topic/alerts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketAlertPublisher {

    private final SimpMessagingTemplate template;

    public void broadcastAlert(AlertDocument alert) {
        template.convertAndSend("/topic/alerts", alert);
    }
}
