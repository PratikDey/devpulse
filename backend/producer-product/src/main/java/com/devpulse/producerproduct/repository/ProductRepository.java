package com.devpulse.producerproduct.repository;

import com.devpulse.producerproduct.model.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<ProductDocument, String> {
}
