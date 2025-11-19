package com.devpulse.producerproduct.controller;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.common.dto.ProductDto;
import com.devpulse.producerproduct.exception.ProductNotFoundException;
import com.devpulse.producerproduct.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ProductController
 *
 * Exposes REST endpoints for product operations.
 * Returns uniform ApiResponse<T> objects for frontend integration consistency.
 *
 * Responsibilities:
 *  - Receive & validate input
 *  - Forward business requests to ProductService
 *  - Structure responses in a consistent format
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /* CREATE --------------------------------------------------------------- */

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@RequestBody ProductDto dto) {
        ProductDto created = productService.createProduct(dto);

        return ResponseEntity.ok(
                ApiResponse.<ProductDto>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(created)
                        .build()
        );
    }

    /* READ: GET ONE -------------------------------------------------------- */

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String id) {

        ProductDto dto = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ResponseEntity.ok(
                ApiResponse.<ProductDto>builder()
                        .success(true)
                        .message("Product fetched successfully")
                        .data(dto)
                        .build()
        );
    }

    /* READ: PAGINATION ----------------------------------------------------- */

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductDto> result = productService.getAllProducts(page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductDto>>builder()
                        .success(true)
                        .message("Products fetched successfully")
                        .data(result)
                        .build()
        );
    }

    /* UPDATE --------------------------------------------------------------- */

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable String id,
            @RequestBody ProductDto update
    ) {

        ProductDto updated = productService.updateProduct(id, update)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ResponseEntity.ok(
                ApiResponse.<ProductDto>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .data(updated)
                        .build()
        );
    }

    /* DELETE --------------------------------------------------------------- */

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteProduct(@PathVariable String id) {

        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            throw new ProductNotFoundException(id);
        }

        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .data(true)
                        .build()
        );
    }
}
