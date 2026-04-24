package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreateProductRequest;
import com.ecommerce.api.dto.request.UpdateProductRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BaseResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request){
        ProductResponse productResponse =  this.productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(productResponse));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getAll() {
        List<ProductResponse> productResponses = this.productService.getAllProducts();
        return ResponseEntity.ok(BaseResponse.success(productResponses));
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
