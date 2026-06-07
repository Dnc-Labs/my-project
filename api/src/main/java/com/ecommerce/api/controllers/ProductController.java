package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.ProductFilterRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.PageResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.security.CustomUserDetails;
import com.ecommerce.api.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BaseResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request, @AuthenticationPrincipal
                                                                CustomUserDetails userDetails){
        ProductResponse productResponse =  this.productService.createProduct(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(productResponse));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<ProductResponse>>> getAll(
            // Spring tự bind ?keyword=...&categoryId=...&minPrice=... vào field của filter
            // (không cần @RequestParam — object non-body được xử lý qua @ModelAttribute ngầm).
            ProductFilterRequest filter,
            Pageable pageable
    ) {
        PageResponse<ProductResponse> pageResponse = productService.getAllProducts(filter, pageable);
        return ResponseEntity.ok(BaseResponse.success(pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse productResponses = this.productService.getProductById(id);
        return ResponseEntity.ok(BaseResponse.success(productResponses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productSecurity.isOwner(#id, authentication))")
    public ResponseEntity<BaseResponse<ProductResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse productResponse = this.productService.updateProduct(id, request);
        return ResponseEntity.ok(BaseResponse.success(productResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productSecurity.isOwner(#id, authentication))")
    public ResponseEntity<BaseResponse<?>> delete(@PathVariable Long id) {
        this.productService.deleteProduct(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
