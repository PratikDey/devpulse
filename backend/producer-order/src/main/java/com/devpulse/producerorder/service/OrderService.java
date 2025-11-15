package com.devpulse.producerorder.service;

import com.devpulse.common.dto.OrderDto;
import com.devpulse.common.enums.LogLevel;
import com.devpulse.producerorder.kafka.LogProducer;
import com.devpulse.producerorder.model.OrderDocument;
import com.devpulse.producerorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * OrderService
 *
 * Business logic for order operations.
 * - Persists order business data into a local Mongo collection (orders).
 * - Emits structured logs to Kafka via LogProducer after each important event.
 *
 * The service keeps business data and telemetry (logs) separated:
 *  - Orders -> Mongo "orders" collection
 *  - Logs   -> Kafka "devpulse-logs" consumed by log-collector
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final LogProducer logProducer;

    private static final String SERVICE_NAME = "producer-order";

    /**
     * Create and persist an order. Also emits an INFO log to Kafka.
     *
     * @param dto incoming OrderDto (from common/)
     * @return saved OrderDto (echo back)
     */
    public OrderDto createOrder(OrderDto dto) {
        // Ensure orderId exists
        String orderId = dto.getOrderId() != null ? dto.getOrderId() : UUID.randomUUID().toString();
        dto.setOrderId(orderId);

        // Save business entity
        OrderDocument doc = OrderDocument.builder()
                .orderId(dto.getOrderId())
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .orderDate(dto.getOrderDate())
                .build();

        orderRepository.save(doc);

        // Publish telemetry log
        String message = "Order created: orderId=" + orderId + ", productId=" + dto.getProductId()
                + ", qty=" + dto.getQuantity();
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, null);

        return dto;
    }

    // You can add update/delete/get methods similarly and send logs accordingly
}
