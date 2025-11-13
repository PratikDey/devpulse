package com.devpulse.common.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String orderId;
    private String productId;
    private int quantity;
    private double price;
    private Instant orderDate;
}