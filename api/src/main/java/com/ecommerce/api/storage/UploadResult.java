package com.ecommerce.api.storage;

/**
 * Kết quả upload file. Java record — immutable, gọn.
 *
 * - key: định danh internal trên storage (ví dụ "products/uuid.jpg")
 *        Dùng để delete sau này. KHÔNG expose ra client.
 * - url: URL public client dùng để truy cập file.
 *        Local: "http://localhost:8081/uploads/products/uuid.jpg"
 *        S3:    "https://bucket.s3.amazonaws.com/products/uuid.jpg"
 */
public record UploadResult(String key, String url) {
}
