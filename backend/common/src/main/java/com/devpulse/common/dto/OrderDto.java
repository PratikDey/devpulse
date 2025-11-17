package com.devpulse.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String orderId;
    @NotBlank(message = "productId is required")
    private String productId;
    @Min(value = 1, message = "quantity must be >= 1")
    private int quantity;
    @PositiveOrZero(message = "price must be >= 0")
    private double price;
    private Instant orderDate;
}