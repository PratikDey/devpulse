package com.devpulse.producerorder.controller;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.common.dto.OrderDto;
import com.devpulse.producerorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OrderController
 *
 * REST API layer for order operations inside the producer-order service.
 *
 * All responses are wrapped in ApiResponse<T> to ensure:
 *  - Consistent response structure across ALL DevPulse microservices
 *  - Easy integration with frontend
 *  - Clear debugging and predictable API design
 *
 * Expected Response Format:
 * {
 *   "success": true/false,
 *   "message": "Status message",
 *   "data": { ... }   // optional
 * }
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order and publishes a log event to Kafka.
     *
     * @param dto Order request payload
     * @return Standardized API response containing the created order
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@RequestBody OrderDto dto) {

        OrderDto savedOrder = orderService.createOrder(dto);

        ApiResponse<OrderDto> response = ApiResponse.<OrderDto>builder()
                .success(true)
                .message("Order created successfully")
                .data(savedOrder)
                .build();

        return ResponseEntity.ok(response);
    }
}
