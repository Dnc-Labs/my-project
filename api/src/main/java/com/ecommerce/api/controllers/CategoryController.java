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

    /**
     * TODO: API tạo category (chỉ ADMIN)
     * - POST /api/categories
     * - Nhận @Valid @RequestBody CreateCategoryRequest
     * - Trả về ResponseEntity 201 + BaseResponse.success()
     * - Phân quyền: @PreAuthorize("hasRole('ADMIN')")
     */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse categoryResponse = this.categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(categoryResponse));
    }

    /**
     * TODO: API lấy tất cả category (dạng tree)
     * - GET /api/categories
     * - Ai cũng xem được (không cần phân quyền)
     * - Trả về BaseResponse<List<CategoryResponse>>
     */

    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categoryResponses = this.categoryService.getAllCategories();
        return ResponseEntity.ok(BaseResponse.success(categoryResponses));
    }
    /**
     * TODO: API lấy chi tiết 1 category
     * - GET /api/categories/{id}
     * - Ai cũng xem được
     * - Trả về BaseResponse<CategoryResponse>
     */

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getById(@PathVariable Long id){
        CategoryResponse categoryResponse = this.categoryService.getCategoryById(id);
        return ResponseEntity.ok(BaseResponse.success(categoryResponse));
    }

    /**
     * TODO: API cập nhật category (chỉ ADMIN)
     * - PUT /api/categories/{id}
     * - Nhận @Valid @RequestBody UpdateCategoryRequest
     * - Trả về BaseResponse<CategoryResponse>
     * - Phân quyền: @PreAuthorize("hasRole('ADMIN')")
     */

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> updateById(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse categoryResponse = this.categoryService.updateCategory(id, request);
        return ResponseEntity.ok(BaseResponse.success(categoryResponse));
    }



    /**
     * TODO: API xoá category (chỉ ADMIN)
     * - DELETE /api/categories/{id}
     * - Trả về BaseResponse (không có data, chỉ message)
     * - Phân quyền: @PreAuthorize("hasRole('ADMIN')")
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> deleteById(@PathVariable Long id) {
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}