package com.devpulse.alertprocessor.repository;

import com.devpulse.alertprocessor.model.AlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlertRepository extends MongoRepository<AlertDocument, String> {
    List<AlertDocument> findByServiceName(String serviceName);
}
