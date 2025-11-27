package com.devpulse.logdashboard.service;

import com.devpulse.common.dto.LogResponseDto;
import com.devpulse.common.enums.LogLevel;
import com.devpulse.logdashboard.model.LogDocument;
import com.devpulse.logdashboard.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LogQueryService
 *
 * Encapsulates DB read logic and maps documents to LogResponseDto used by the API.
 */
@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final LogRepository repository;

    public Page<LogResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogDocument> docs = repository.findAll(pageable);
        return docs.map(this::toDto);
    }

    public Page<LogResponseDto> findByService(String serviceName, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogDocument> docs = repository.findByServiceName(serviceName, pageable);
        return docs.map(this::toDto);
    }

    public Page<LogResponseDto> findByLevel(String levelStr, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "timestamp"));
        try {
            var level = LogLevel.valueOf(levelStr.toUpperCase());
            Page<LogDocument> docs = repository.findByLevel(level, pageable);
            return docs.map(this::toDto);
        } catch (IllegalArgumentException ex) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    public Page<LogResponseDto> findBetween(InstantRange range, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogDocument> docs = repository.findByTimestampBetween(range.from(), range.to(), pageable);
        return docs.map(this::toDto);
    }

    public List<LogResponseDto> recentTop100() {
        return repository.findTop100ByOrderByTimestampDesc().stream().map(this::toDto).collect(Collectors.toList());
    }

    private LogResponseDto toDto(LogDocument doc) {
        return LogResponseDto.builder()
                .id(doc.getId())
                .serviceName(doc.getServiceName())
                .level(doc.getLevel())
                .message(doc.getMessage())
                .timestamp(doc.getTimestamp())
                .traceId(doc.getTraceId())
                .build();
    }

    /**
     * small helper for timestamp range
     */
        public record InstantRange(Instant from, Instant to) {
    }
}
