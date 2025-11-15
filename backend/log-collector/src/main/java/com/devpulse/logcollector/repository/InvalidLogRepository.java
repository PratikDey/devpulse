package com.devpulse.logcollector.repository;

import com.devpulse.logcollector.model.InvalidLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface InvalidLogRepository extends MongoRepository<InvalidLogDocument, String> {
}
