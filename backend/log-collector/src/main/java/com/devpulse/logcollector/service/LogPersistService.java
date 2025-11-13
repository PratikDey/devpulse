package com.devpulse.logcollector.service;

import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.logcollector.model.LogDocument;
import com.devpulse.logcollector.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogPersistService {

    private final LogRepository logRepository;

    public void saveLog(LogMessageDto dto) {
        LogDocument doc = LogDocument.builder()
                .serviceName(dto.getServiceName())
                .message(dto.getMessage())
                .level(dto.getLevel() != null ? dto.getLevel().name() : null)
                .timestamp(dto.getTimestamp())
                .traceId(dto.getTraceId())
                .build();

        logRepository.save(doc);
    }
}