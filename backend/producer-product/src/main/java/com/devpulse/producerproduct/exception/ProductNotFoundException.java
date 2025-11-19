package com.devpulse.producerproduct.exception;

/**
 * Thrown when a requested product does not exist.
 * This is a domain-level exception handled by GlobalExceptionHandler.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product not found with id: " + productId);
    }
}
