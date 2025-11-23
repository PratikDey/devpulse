package com.devpulse.logdashboard.service;

import com.devpulse.common.dto.LogResponseDto;
import com.devpulse.logdashboard.model.LogDocument;
import com.devpulse.logdashboard.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

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
            var level = com.devpulse.common.enums.LogLevel.valueOf(levelStr.toUpperCase());
            Page<LogDocument> docs = repository.findByLevel(level, pageable);
            return docs.map(this::toDto);
        } catch (IllegalArgumentException ex) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    public Page<LogResponseDto> findBetween(InstantRange range, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogDocument> docs = repository.findByTimestampBetween(range.getFrom(), range.getTo(), pageable);
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

    /** small helper for timestamp range */
    public static class InstantRange {
        private final java.time.Instant from;
        private final java.time.Instant to;
        public InstantRange(java.time.Instant from, java.time.Instant to) { this.from = from; this.to = to; }
        public java.time.Instant getFrom() { return from; }
        public java.time.Instant getTo() { return to; }
    }
}
