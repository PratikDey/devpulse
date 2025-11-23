package com.devpulse.logdashboard.repository;

import com.devpulse.logdashboard.model.LogDocument;
import com.devpulse.common.enums.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface LogRepository extends MongoRepository<LogDocument, String> {

    Page<LogDocument> findByServiceName(String serviceName, Pageable pageable);

    Page<LogDocument> findByLevel(LogLevel level, Pageable pageable);

    Page<LogDocument> findByServiceNameAndLevel(String serviceName, LogLevel level, Pageable pageable);

    Page<LogDocument> findByTimestampBetween(Instant from, Instant to, Pageable pageable);

    // Convenience
    List<LogDocument> findTop100ByOrderByTimestampDesc();
}
