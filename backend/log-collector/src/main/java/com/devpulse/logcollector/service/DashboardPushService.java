package com.devpulse.logcollector.service;

import com.devpulse.common.dto.LogMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardPushService {

    private final RestTemplate restTemplate;

    @Value("${app.dashboard-url}")
    private String dashboardUrl;

    public void pushLog(LogMessageDto logDto) {
        try {
            String url = dashboardUrl + "/api/logs/push";
            restTemplate.postForObject(url, logDto, Void.class);
            log.debug("Pushed log to dashboard: {}", logDto.getTraceId());
        } catch (Exception e) {
            // We log error but don't throw it, so we don't block the consumer
            log.warn("Failed to push log to dashboard: {}", e.getMessage());
        }
    }
}
