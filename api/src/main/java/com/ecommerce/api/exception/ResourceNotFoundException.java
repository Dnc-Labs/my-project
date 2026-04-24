package com.ecommerce.api.exception;

/**
 * Exception khi không tìm thấy resource (User, Product, Order...).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
