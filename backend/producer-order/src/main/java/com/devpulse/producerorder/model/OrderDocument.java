package com.devpulse.producerorder.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document that stores order business data.
 * Note: logs are NOT stored here; logs go to Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
public class OrderDocument {
    @Id
    private String orderId;
    private String name;
    private String productId;
    private int quantity;
    private double price;
    private Instant orderDate;
}
