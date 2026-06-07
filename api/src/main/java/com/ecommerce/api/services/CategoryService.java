package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateCategoryRequest;
import com.ecommerce.api.dto.request.UpdateCategoryRequest;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.exception.BusinessRuleException;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.CategoryMapper;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.utilities.CheckData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class CategoryService {

    // Circuit breaker: nếu DB lỡ có cycle (bug cũ hoặc race), walk-up sẽ vô tận.
    // 100 cấp thừa sức cho mọi category tree e-commerce thực tế.
    private static final int MAX_TREE_DEPTH = 100;

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        boolean checkExistsSlug = this.categoryRepository.existsBySlug(request.getSlug());
        if(checkExistsSlug) throw new DuplicateResource("Slug already exists");
        log.info("Created new category : {}", request.getSlug());
        Category existParent = null;
        if(request.getParentId() != null) {
            existParent = this.categoryRepository.findById(request.getParentId()).orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        Category category = categoryMapper.fromRequestDto(request);
        if(existParent != null) {
            category.setParent(existParent);
        }
        Category created = this.categoryRepository.save(category);
        log.info("Category created successfully: id={}", created.getId());
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

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Update category with slug: {}", request.getSlug());
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
            if(parentCategory.getId().equals(category.getId())) throw new BusinessRuleException("Cannot set category as its own parent");
            if (isAncestorOf(parentCategory, category.getId())) {
                throw new BusinessRuleException("Cannot set a descendant as parent (circular reference)");
            }
            category.setParent(parentCategory);
        }
        log.info("Category update successfully: id={}", id);
        return categoryMapper.fromEntity(this.categoryRepository.save(category));
    }

    /**
     * Xoá category: chặn nếu còn children (admin phải xoá con trước để
     * tránh xoá nhầm cả cây danh mục).
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if(CheckData.checkIsNotNull(category.getChildren())) throw new BusinessRuleException("Cannot delete category that has children");
        this.categoryRepository.delete(category);
        log.info("Category delete successfully: id={}", id);
    }

    /**
     * Kiểm tra `categoryId` có nằm trong chuỗi tổ tiên của `newParent` không.
     * Walk-up: newParent → parent → parent.parent → ... → root.
     * Return true nếu trong chain gặp node có id == categoryId (= cycle).
     *
     * Why walk-up: category tree e-commerce thường rộng-nông (H << N),
     * walk-up tốn O(H) query, walk-down tốn O(N).
     */
    private boolean isAncestorOf(Category newParent, Long categoryId) {
        Category current = newParent;
        int depth = 0;
        while (current != null) {
            if (++depth > MAX_TREE_DEPTH) {
                throw new IllegalStateException(
                    "Category tree exceeds max depth " + MAX_TREE_DEPTH
                        + " — possible pre-existing cycle starting at id=" + newParent.getId());
            }
            if (current.getId().equals(categoryId)) return true;
            current = current.getParent();
        }
        return false;
    }
}
