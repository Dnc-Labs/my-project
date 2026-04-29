package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreateProductVariantRequest;
import com.ecommerce.api.dto.request.UpdateProductVariantRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.ProductVariantResponse;
import com.ecommerce.api.services.ProductVariantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Variant API dùng pattern HYBRID:
 *
 *   POST   /api/products/{productId}/variants    — tạo (cần biết product)
 *   GET    /api/products/{productId}/variants    — list của 1 product
 *   GET    /api/variants/{id}                     — detail (chỉ cần variantId)
 *   PUT    /api/variants/{id}                     — update
 *   DELETE /api/variants/{id}                     — delete
 *
 * Class này không có @RequestMapping ở class level vì endpoint dùng 2 base URL.
 * Mỗi method khai báo path đầy đủ.
 */
@RestController
public class ProductVariantController {

    private final ProductVariantService variantService;

    public ProductVariantController(ProductVariantService variantService) {
        this.variantService = variantService;
    }

    @PostMapping("/api/products/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productSecurity.isOwner(#productId, authentication))")
    public ResponseEntity<BaseResponse<ProductVariantResponse>> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductVariantRequest request) {
        ProductVariantResponse response = variantService.createVariant(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/api/products/{productId}/variants")
    public ResponseEntity<BaseResponse<List<ProductVariantResponse>>> getVariantsByProduct(@PathVariable Long productId) {
        List<ProductVariantResponse> variants = variantService.getVariantsByProduct(productId);
        return ResponseEntity.ok(BaseResponse.success(variants));
    }

    @GetMapping("/api/variants/{id}")
    public ResponseEntity<BaseResponse<ProductVariantResponse>> getById(@PathVariable Long id) {
        ProductVariantResponse response = variantService.getVariantById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/api/variants/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productVariantSecurity.isOwner(#id, authentication))")
    public ResponseEntity<BaseResponse<ProductVariantResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductVariantRequest request) {
        ProductVariantResponse response = variantService.updateVariant(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/api/variants/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productVariantSecurity.isOwner(#id, authentication))")
    public ResponseEntity<BaseResponse<?>> delete(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
