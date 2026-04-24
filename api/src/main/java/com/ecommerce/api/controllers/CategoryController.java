package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreateCategoryRequest;
import com.ecommerce.api.dto.request.UpdateCategoryRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse categoryResponse = this.categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(categoryResponse));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categoryResponses = this.categoryService.getAllCategories();
        return ResponseEntity.ok(BaseResponse.success(categoryResponses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getById(@PathVariable Long id){
        CategoryResponse categoryResponse = this.categoryService.getCategoryById(id);
        return ResponseEntity.ok(BaseResponse.success(categoryResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> updateById(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse categoryResponse = this.categoryService.updateCategory(id, request);
        return ResponseEntity.ok(BaseResponse.success(categoryResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> deleteById(@PathVariable Long id) {
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}