package com.devpulse.producerorder.controller;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.common.dto.OrderDto;
import com.devpulse.producerorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Get all orders (paged)
     * Query params: ?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<OrderDto> pageResult = orderService.getAllOrders(page, size);
        ApiResponse<Page<OrderDto>> resp = ApiResponse.<Page<OrderDto>>builder()
                .success(true)
                .message("Orders fetched")
                .data(pageResult)
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * Get all orders of a given product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrderOfOneProduct(@PathVariable String productId) {
        List<OrderDto> list = orderService.getOrdersByProductId(productId);
        ApiResponse<List<OrderDto>> resp = ApiResponse.<List<OrderDto>>builder()
                .success(true)
                .message("Orders for product " + productId)
                .data(list)
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * Update one order (replace fields provided in body)
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> updateOneOrder(@PathVariable String orderId,
                                                                @RequestBody OrderDto dto) {
        return orderService.updateOrder(orderId, dto)
                .map(updated -> {
                    ApiResponse<OrderDto> resp = ApiResponse.<OrderDto>builder()
                            .success(true)
                            .message("Order updated successfully")
                            .data(updated)
                            .build();
                    return ResponseEntity.ok(resp);
                })
                .orElseGet(() -> {
                    ApiResponse<OrderDto> resp = ApiResponse.<OrderDto>builder()
                            .success(false)
                            .message("Order not found: " + orderId)
                            .data(null)
                            .build();
                    return ResponseEntity.status(404).body(resp);
                });
    }

    /**
     * Delete an order
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOneOrder(@PathVariable String orderId) {
        boolean deleted = orderService.deleteOrder(orderId);
        if (deleted) {
            ApiResponse<Void> resp = ApiResponse.<Void>builder()
                    .success(true)
                    .message("Order deleted: " + orderId)
                    .data(null)
                    .build();
            return ResponseEntity.ok(resp);
        } else {
            ApiResponse<Void> resp = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Order not found: " + orderId)
                    .data(null)
                    .build();
            return ResponseEntity.status(404).body(resp);
        }
    }
}
