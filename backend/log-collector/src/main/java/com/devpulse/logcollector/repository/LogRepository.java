package com.devpulse.logcollector.repository;

import com.devpulse.logcollector.model.LogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<LogDocument, String> {
}