package com.ecommerce.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.ProductImageResponse;
import com.ecommerce.api.services.ProductImageService;

/**
 * Image API dùng pattern HYBRID giống Variant:
 * <p>
 * POST   /api/products/{productId}/images   — upload (multipart)
 * GET    /api/products/{productId}/images   — list (public)
 * PUT    /api/images/{id}/primary           — set ảnh primary
 * DELETE /api/images/{id}                    — xoá
 */
@RestController
@RequestMapping("/api")
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/products/{productId}/images")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productSecurity.isOwner(#productId, authentication))")
    public ResponseEntity<BaseResponse<ProductImageResponse>> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        ProductImageResponse image = this.imageService.uploadImage(productId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(image));
    }


    @GetMapping("/products/{productId}/images")
    public ResponseEntity<BaseResponse<List<ProductImageResponse>>> getImages(@PathVariable Long productId) {
        List<ProductImageResponse> image = this.imageService.getImagesByProduct(productId);
        return ResponseEntity.ok(BaseResponse.success(image));
    }

    @PutMapping("/images/{id}/primary")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productImageSecurity.isOwner(#id, authentication))")
    public ResponseEntity<BaseResponse<ProductImageResponse>> setPrimaryImage(@PathVariable Long id) {
        ProductImageResponse image = imageService.setPrimary(id);
        return ResponseEntity.ok(BaseResponse.success(image));
    }

    @DeleteMapping("/images/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productImageSecurity.isOwner(#id, authentication))")
    public ResponseEntity<BaseResponse<Void>> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
