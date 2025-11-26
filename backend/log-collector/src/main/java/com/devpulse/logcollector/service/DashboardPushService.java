package com.devpulse.logcollector.service;

import com.devpulse.logcollector.client.DashboardClient;
import com.devpulse.common.dto.LogMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardPushService {

    private final DashboardClient dashboardClient;

    public void pushLog(LogMessageDto logDto) {
        try {
            dashboardClient.pushLog(logDto);
            log.debug("Pushed log to dashboard: {}", logDto.getTraceId());
        } catch (Exception e) {
            // We log error but don't throw it, so we don't block the consumer
            log.warn("Failed to push log to dashboard: {}", e.getMessage());
        }
    }
}
