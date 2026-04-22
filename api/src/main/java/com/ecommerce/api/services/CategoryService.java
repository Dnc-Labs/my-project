package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateCategoryRequest;
import com.ecommerce.api.dto.request.UpdateCategoryRequest;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.utilities.CheckData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * TODO: Tạo category mới
     * 1. Kiểm tra slug đã tồn tại chưa → nếu rồi thì throw exception
     * 2. Nếu có parentId → tìm parent category, nếu không tìm thấy thì throw ResourceNotFoundException
     * 3. Tạo Category entity từ request, set parent nếu có
     * 4. Lưu vào database
     * 5. Trả về CategoryResponse
     */
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        boolean checkExistsSlug = this.categoryRepository.existsBySlug(request.getSlug());
        if(checkExistsSlug) throw new RuntimeException("Slug already exists");
        Category existParent = null;
        if(request.getParentId() != null) {
            existParent = this.categoryRepository.findById(request.getParentId()).orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
        if(existParent != null) {
            category.setParent(existParent);
        }
        Category created = this.categoryRepository.save(category);
        return CategoryResponse.fromEntity(created);
    }

    /**
     * TODO: Lấy tất cả category dạng tree
     * 1. Tìm tất cả category gốc (parent == null)
     * 2. Convert sang List<CategoryResponse> (fromEntity sẽ tự đệ quy lấy children)
     * 3. Trả về list
     */
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = this.categoryRepository.findByParentIsNull();
        return categories.stream().map(CategoryResponse::fromEntity).toList();
    }

    /**
     * TODO: Lấy chi tiết 1 category theo id
     * 1. Tìm category theo id → nếu không có thì throw ResourceNotFoundException
     * 2. Trả về CategoryResponse
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return CategoryResponse.fromEntity(category);
    }

    /**
     * TODO: Cập nhật category
     * 1. Tìm category theo id → nếu không có thì throw ResourceNotFoundException
     * 2. Nếu đổi slug → kiểm tra slug mới đã tồn tại chưa
     * 3. Nếu đổi parentId → tìm parent mới, kiểm tra không cho phép set parent = chính nó
     * 4. Cập nhật các field từ request (chỉ update field nào không null)
     * 5. Lưu vào database
     * 6. Trả về CategoryResponse
     */
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        String slugUpdate = request.getSlug();
        if(CheckData.checkIsNotNull(slugUpdate) && !slugUpdate.equals(category.getSlug())) {
            boolean checkExistedBySlug = this.categoryRepository.existsBySlug(slugUpdate);
            if(checkExistedBySlug) throw new RuntimeException("Slug already exists");
            category.setSlug(slugUpdate);
        }
        Long parentId = request.getParentId();

        if(CheckData.checkIsNotNull(parentId)) {
            Category parentCategory = this.categoryRepository.findById(parentId).orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            if(parentCategory.getId().equals(category.getId())) throw new RuntimeException("Cannot set category as its own parent");
            if (CheckData.checkIsNotNull(category.getChildren())) {
                boolean check = category.getChildren().stream().anyMatch(c -> c.getId().equals(parentId));
                if (check) throw new RuntimeException("Cannot set a descendant as parent (circular reference)");
            }
            category.setParent(parentCategory);
        }

        if(CheckData.checkIsNotNull(request.getName())) {
            category.setName(request.getName());
        }

        if(CheckData.checkIsNotNull(request.getDescription())) {
            category.setDescription(request.getDescription());
        }

        if(CheckData.checkIsNotNull(request.getStatus())) {
            category.setStatus(request.getStatus());
        }

        return CategoryResponse.fromEntity(this.categoryRepository.save(category));
    }

    /**
     * TODO: Xoá category
     * 1. Tìm category theo id → nếu không có thì throw ResourceNotFoundException
     * 2. Kiểm tra xem category có children không → nếu có thì CHẶN, throw exception
     *    (Lý do: tránh xoá nhầm cả cây danh mục, admin phải xoá con trước)
     * 3. Xoá category
     */
    public void deleteCategory(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if(CheckData.checkIsNotNull(category.getChildren())) throw new RuntimeException("Cannot delete category that has children");
        this.categoryRepository.deleteById(id);
    }
}