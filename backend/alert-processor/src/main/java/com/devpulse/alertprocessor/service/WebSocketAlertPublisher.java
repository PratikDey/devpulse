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

    /**
     * Publish saved alerts to frontend subscribers over STOMP/WebSocket.
     *
     * Sends the provided {@link com.devpulse.alertprocessor.model.AlertDocument} to the /topic/alerts destination using
     * the configured SimpMessagingTemplate. Exceptions thrown by the messaging template will propagate to the caller;
     * callers should handle and log failures when necessary.
     *
     * @param alert the persisted alert document to broadcast
     */

    public void broadcastAlert(AlertDocument alert) {
        template.convertAndSend("/topic/alerts", alert);
    }
}
