package com.devpulse.producerproduct.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "products")
public class ProductDocument {

    @Id
    private String productId;

    private String name;
    private String category;
    private double price;
    private Instant createdAt;
}
