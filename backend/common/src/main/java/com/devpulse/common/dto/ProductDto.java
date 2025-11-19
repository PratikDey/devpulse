package com.devpulse.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private String productId;

    @NotBlank(message = "Product name is required")
    private String name;
    private String category;

    @PositiveOrZero(message = "Price must be >= 0")
    private double price;

    private Instant createdAt;
}