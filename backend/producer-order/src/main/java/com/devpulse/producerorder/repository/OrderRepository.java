package com.devpulse.producerorder.repository;

import com.devpulse.producerorder.model.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Simple Spring Data repository for orders.
 * We keep it trivial for hackathon speed; extend later for queries.
 */
public interface OrderRepository extends MongoRepository<OrderDocument, String> {
}
