package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateCategoryRequest;
import com.ecommerce.api.dto.request.UpdateCategoryRequest;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.CategoryMapper;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.utilities.CheckData;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        boolean checkExistsSlug = this.categoryRepository.existsBySlug(request.getSlug());
        if(checkExistsSlug) throw new DuplicateResource("Slug already exists");
        Category existParent = null;
        if(request.getParentId() != null) {
            existParent = this.categoryRepository.findById(request.getParentId()).orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        Category category = categoryMapper.fromRequestDto(request);
        if(existParent != null) {
            category.setParent(existParent);
        }
        Category created = this.categoryRepository.save(category);
        return categoryMapper.fromEntity(created);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = this.categoryRepository.findByParentIsNull();
        return categories.stream().map(categoryMapper::fromEntity).toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return categoryMapper.fromEntity(category);
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryMapper.updateEntity(request, category);
        String slugUpdate = request.getSlug();
        if(CheckData.checkIsNotNull(slugUpdate) && !slugUpdate.equals(category.getSlug())) {
            boolean checkExistedBySlug = this.categoryRepository.existsBySlug(slugUpdate);
            if(checkExistedBySlug) throw new DuplicateResource("Slug already exists");
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
        return categoryMapper.fromEntity(this.categoryRepository.save(category));
    }

    /**
     * Xoá category: chặn nếu còn children (admin phải xoá con trước để
     * tránh xoá nhầm cả cây danh mục).
     */
    public void deleteCategory(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if(CheckData.checkIsNotNull(category.getChildren())) throw new RuntimeException("Cannot delete category that has children");
        this.categoryRepository.deleteById(id);
    }
}
