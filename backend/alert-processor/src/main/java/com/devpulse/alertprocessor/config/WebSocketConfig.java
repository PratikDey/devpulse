package com.devpulse.alertprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * STOMP over WebSocket. Frontend subscribes to /topic/alerts.
 */

/**
 * WebSocket/STOMP configuration for frontend subscriptions.
 *
 * Key points:
 *  - Registers endpoint `/alert-ws` with SockJS fallback; allowed origins are configured via the `websocket.allowed-origins` property.
 *  - Uses Spring's simple in-memory broker for destinations prefixed with `/topic` (suitable for MVP).
 *  - For production-scale messaging, replace the simple broker with a full-featured broker (RabbitMQ, ActiveMQ, etc.).
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/alert-ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broket for MVP; replace with full broker if needed later
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
