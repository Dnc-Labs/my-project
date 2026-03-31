package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.ProductRequestDto;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(this.productService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(this.productService.getAllProducts());
    }


    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequestDto productRequestDto) {
        Product product = this.productService.create(productRequestDto);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(this.productService.deleteProduct(id));
    }
}
