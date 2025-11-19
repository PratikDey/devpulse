package com.devpulse.producerproduct.service;

import com.devpulse.common.dto.ProductDto;
import com.devpulse.common.enums.LogLevel;
import com.devpulse.producerproduct.kafka.LogProducer;
import com.devpulse.producerproduct.model.ProductDocument;
import com.devpulse.producerproduct.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductService
 *
 * Business logic for product operations.
 * - Persists product business data into MongoDB ("products" collection)
 * - Emits structured logs to Kafka using LogProducer
 *
 * Consistency with OrderService is intentional:
 *   ProductService = business logic only
 *   LogProducer    = telemetry/logging only
 *
 * This keeps separation of concerns very clean and consistent across microservices.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final LogProducer logProducer;

    private static final String SERVICE_NAME = "producer-product";

    /* HELPERS ---------------------------------------------------------------- */

    private ProductDto toDto(ProductDocument doc) {
        if (doc == null) return null;
        return ProductDto.builder()
                .productId(doc.getProductId())
                .name(doc.getName())
                .category(doc.getCategory())
                .price(doc.getPrice())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private ProductDocument toDocument(ProductDto dto) {
        return ProductDocument.builder()
                .productId(dto.getProductId())
                .name(dto.getName())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    /* CREATE ----------------------------------------------------------------- */

    /**
     * Creates a new product and stores it in MongoDB.
     * Also emits an INFO log to Kafka.
     *
     * @param dto incoming Product DTO
     * @return saved ProductDto (as confirmation)
     */
    public ProductDto createProduct(ProductDto dto) {

        // Ensure productId exists
        String productId = dto.getProductId() != null ? dto.getProductId() : UUID.randomUUID().toString();
        dto.setProductId(productId);
        if (dto.getCreatedAt() == null) {
            dto.setCreatedAt(Instant.now());
        }

        // Persist business entity
        ProductDocument doc = toDocument(dto);
        productRepository.save(doc);

        // Emit log
        String message = "Product created: productId=" + productId + ", name=" + dto.getName();
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, productId);

        return dto;
    }

    /* READ: GET ONE --------------------------------------------------------- */

    public Optional<ProductDto> getProductById(String productId) {
        return productRepository.findById(productId).map(this::toDto);
    }

    /* READ: PAGINATION ------------------------------------------------------ */

    public Page<ProductDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<ProductDocument> docs = productRepository.findAll(pageable);

        List<ProductDto> dtos = docs.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, docs.getTotalElements());
    }

    /* UPDATE ---------------------------------------------------------------- */

    public Optional<ProductDto> updateProduct(String productId, ProductDto update) {

        Optional<ProductDocument> existingOpt = productRepository.findById(productId);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        ProductDocument existing = existingOpt.get();

        // Apply updates (simple overwrite strategy)
        existing.setName(update.getName() != null ? update.getName() : existing.getName());
        existing.setCategory(update.getCategory() != null ? update.getCategory() : existing.getCategory());
        existing.setPrice(update.getPrice() != 0.0 ? update.getPrice() : existing.getPrice());

        productRepository.save(existing);

        // Emit update log
        String message = "Product updated: productId=" + productId + ", name=" + existing.getName();
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, productId);

        return Optional.of(toDto(existing));
    }

    /* DELETE ---------------------------------------------------------------- */

    public boolean deleteProduct(String productId) {

        Optional<ProductDocument> existingOpt = productRepository.findById(productId);
        if (existingOpt.isEmpty()) {
            return false;
        }

        productRepository.deleteById(productId);

        // Emit delete log
        String message = "Product deleted: productId=" + productId;
        logProducer.sendLog(SERVICE_NAME, LogLevel.INFO, message, productId);

        return true;
    }
}
