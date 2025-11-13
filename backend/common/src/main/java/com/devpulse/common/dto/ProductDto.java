package com.devpulse.common.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private String productId;
    private String name;
    private String category;
    private double price;
    private Instant createdAt;
}