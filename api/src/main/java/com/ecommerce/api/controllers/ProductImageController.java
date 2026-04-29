package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.ProductImageResponse;
import com.ecommerce.api.services.ProductImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Image API dùng pattern HYBRID giống Variant:
 *
 *   POST   /api/products/{productId}/images   — upload (multipart)
 *   GET    /api/products/{productId}/images   — list (public)
 *   PUT    /api/images/{id}/primary           — set ảnh primary
 *   DELETE /api/images/{id}                    — xoá
 */
@RestController
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * TODO: Upload 1 ảnh cho product (multipart/form-data)
     *
     * - POST /api/products/{productId}/images
     * - @PreAuthorize: ADMIN hoặc SELLER chủ product
     * - Tham số: @RequestParam("file") MultipartFile file
     *   (KHÔNG dùng @RequestBody — multipart không phải JSON)
     * - Trả 201 + BaseResponse<ProductImageResponse>
     *
     * Form-data key phải đúng "file" để FE biết gửi đúng tên field.
     */

    /**
     * TODO: List ảnh của product (public)
     * - GET /api/products/{productId}/images
     */

    /**
     * TODO: Set ảnh primary (ADMIN hoặc SELLER chủ ảnh)
     * - PUT /api/images/{id}/primary
     * - @PreAuthorize: hasRole('ADMIN') or @productImageSecurity.isOwner(...)
     */

    /**
     * TODO: Xoá ảnh (ADMIN hoặc SELLER chủ ảnh)
     * - DELETE /api/images/{id}
     */
}
