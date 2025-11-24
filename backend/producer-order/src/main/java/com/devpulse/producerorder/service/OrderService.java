package com.devpulse.producerorder.service;

import com.devpulse.common.dto.OrderDto;
import com.devpulse.common.enums.LogLevel;
import com.devpulse.producerorder.kafka.LogProducer;
import com.devpulse.producerorder.model.OrderDocument;
import com.devpulse.producerorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OrderService
 *
 * Business logic for order operations.
 * - Persists order business data into a local Mongo collection (orders).
 * - Emits structured logs to Kafka via LogProducer after each important event.
 *
 * The service keeps business data and telemetry (logs) separated:
 * - Orders -> Mongo "orders" collection
 * - Logs -> Kafka "devpulse-logs" consumed by log-collector
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final LogProducer logProducer;

    private static final String SERVICE_NAME = "producer-order";

    /* HELPERS ------------------------------------------------------------- */

    private OrderDto toDto(OrderDocument doc) {
        if (doc == null)
            return null;
        return OrderDto.builder()
                .orderId(doc.getOrderId())
                .name(doc.getName())
                .productId(doc.getProductId())
                .quantity(doc.getQuantity())
                .price(doc.getPrice())
                .orderDate(doc.getOrderDate())
                .build();
    }

    private OrderDocument toDocument(OrderDto dto) {
        return OrderDocument.builder()
                .orderId(dto.getOrderId())
                .name(dto.getName())
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .orderDate(dto.getOrderDate())
                .build();
    }

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
        if (dto.getOrderDate() == null)
            dto.setOrderDate(Instant.now());
        // Save business entity
        OrderDocument doc = toDocument(dto);

        orderRepository.save(doc);

        // Publish telemetry log
        String message = "Order created: orderId=" + orderId + ", productId=" + dto.getProductId()
                + ", qty=" + dto.getQuantity();
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, null);

        return dto;
    }

    /* READ: get one order by id (optional helper) --------------------------- */
    public Optional<OrderDto> getOrderById(String orderId) {
        return orderRepository.findById(orderId).map(this::toDto);
    }

    /* READ: getAllOrders with pagination ----------------------------------- */
    public Page<OrderDto> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<OrderDocument> docs = orderRepository.findAll(pageable);
        List<OrderDto> dtos = docs.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, docs.getTotalElements());
    }

    /* READ: get orders by productId --------------------------------------- */
    public List<OrderDto> getOrdersByProductId(String productId) {
        List<OrderDocument> docs = orderRepository.findByProductId(productId);
        return docs.stream().map(this::toDto).collect(Collectors.toList());
    }

    /* UPDATE ---------------------------------------------------------------- */
    public Optional<OrderDto> updateOrder(String orderId, OrderDto update) {
        Optional<OrderDocument> existingOpt = orderRepository.findById(orderId);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        OrderDocument existing = existingOpt.get();
        // Apply updates (only update provided fields; here we overwrite fields for
        // simplicity)
        existing.setName(update.getName() != null ? update.getName() : existing.getName());
        existing.setProductId(update.getProductId() != null ? update.getProductId() : existing.getProductId());
        existing.setQuantity(update.getQuantity() != 0 ? update.getQuantity() : existing.getQuantity());
        existing.setPrice(update.getPrice() != 0.0 ? update.getPrice() : existing.getPrice());
        existing.setOrderDate(update.getOrderDate() != null ? update.getOrderDate() : existing.getOrderDate());

        orderRepository.save(existing);

        // Emit update log
        String message = "Order updated: orderId=" + orderId + ", productId=" + existing.getProductId()
                + ", qty=" + existing.getQuantity();
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, orderId);

        return Optional.of(toDto(existing));
    }

    /* DELETE ---------------------------------------------------------------- */
    public boolean deleteOrder(String orderId) {
        Optional<OrderDocument> existingOpt = orderRepository.findById(orderId);
        if (existingOpt.isEmpty()) {
            return false;
        }

        orderRepository.deleteById(orderId);

        // Emit delete log
        String message = "Order deleted: orderId=" + orderId;
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, orderId);

        return true;
    }
}
