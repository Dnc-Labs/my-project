package com.ecommerce.api.exception;

/**
 * Exception khi không tìm thấy resource (User, Product, Order...).
 *
 * TODO: Triển khai class này
 * - Extends RuntimeException
 * - Constructor nhận message (String)
 * - Gọi super(message)
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
