package com.devpulse.alertprocessor.repository;

import com.devpulse.alertprocessor.model.AlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for persisting and querying
 * {@link com.devpulse.alertprocessor.model.AlertDocument}.
 *
 * Extend this interface with custom query methods (derived queries or @Query)
 * if you need additional lookup
 * capabilities (by serviceName, severity, ruleId, timestamp ranges, etc.).
 */

public interface AlertRepository extends MongoRepository<AlertDocument, String> {
}
