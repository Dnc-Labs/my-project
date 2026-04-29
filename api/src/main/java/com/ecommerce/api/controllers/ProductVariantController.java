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
 * Class này phải được split làm 2 vì @RequestMapping khác base URL.
 * Để đơn giản, mình gộp vào 1 class — endpoint riêng có path đầy đủ.
 */
@RestController
public class ProductVariantController {

    private final ProductVariantService variantService;

    public ProductVariantController(ProductVariantService variantService) {
        this.variantService = variantService;
    }

    /**
     * TODO: Tạo variant trong product (SELLER chủ product hoặc ADMIN)
     * - POST /api/products/{productId}/variants
     * - Cần check ownership: @productSecurity.isOwner(#productId, authentication)
     * - @Valid @RequestBody CreateProductVariantRequest
     * - Trả ResponseEntity 201 + BaseResponse.success
     */

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productSecurity.isOwner(#productId, authentication))")
    @PostMapping("/api/products/{productId}/variants")
    public ResponseEntity<BaseResponse<ProductVariantResponse>> createVariantProduct(@PathVariable Long productId,@Valid  @RequestBody CreateProductVariantRequest createProductVariantRequest) {
         ProductVariantResponse productVariantResponse =  this.variantService.createVariant(productId, createProductVariantRequest);
         return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(productVariantResponse));
    }


    /**
     * TODO: List variants của product (public)
     * - GET /api/products/{productId}/variants
     * - Không cần @PreAuthorize, đã được SecurityConfig permit
     */

    @GetMapping("/api/products/{productId}/variants")
    public ResponseEntity<BaseResponse<List<ProductVariantResponse>>> getListVariants(@PathVariable Long productId) {
        List<ProductVariantResponse> productVariantResponses = this.variantService.getVariantsByProduct(productId);
        return ResponseEntity.ok(BaseResponse.success(productVariantResponses));
    }

    /**
     * TODO: Detail variant (public)
     * - GET /api/variants/{id}
     * - Cần update SecurityConfig: permit GET /api/variants/**
     */

    @GetMapping("/api/variants/{id}")
    public ResponseEntity<BaseResponse<ProductVariantResponse>> getDetailVariant(@PathVariable Long id) {
        ProductVariantResponse productVariantResponse = this.variantService.getVariantById(id);
        return ResponseEntity.ok(BaseResponse.success(productVariantResponse));
    }
    /**
     * TODO: Update variant (ADMIN hoặc SELLER chủ variant)
     * - PUT /api/variants/{id}
     * - @PreAuthorize("hasRole('ADMIN') or
     *     (hasRole('SELLER') and @productVariantSecurity.isOwner(#id, authentication))")
     */

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productVariantSecurity.isOwner(#id, authentication))")
    @PutMapping("/api/variants/{id}")
    public ResponseEntity<BaseResponse<ProductVariantResponse>> updateVariant(@PathVariable Long id, @Valid @RequestBody UpdateProductVariantRequest request) {
        ProductVariantResponse productVariantResponse = this.variantService.updateVariant(id, request);
        return ResponseEntity.ok(BaseResponse.success(productVariantResponse));
    }

    /**
     * TODO: Delete variant (ADMIN hoặc SELLER chủ variant)
     * - DELETE /api/variants/{id}
     */

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productVariantSecurity.isOwner(#id, authentication))")
    @DeleteMapping("/api/variants/{id}")
    public ResponseEntity<BaseResponse<?>> deleteVariant(@PathVariable Long id) {
        this.variantService.deleteVariant(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
