package com.ecommerce.api.dto.response;

import com.ecommerce.api.entity.Category;
import com.ecommerce.api.enums.CategoryStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO trả về cho client — dạng tree (có chứa children).
 * fromEntity gọi đệ quy để build tree.
 */
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private CategoryStatus status;
    private List<CategoryResponse> children;


    public static CategoryResponse fromEntity(Category category) {
        List<CategoryResponse> childResponses = new ArrayList<>();

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            childResponses = category.getChildren().stream()
                    .map(CategoryResponse::fromEntity)
                    .toList();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getStatus(),
                childResponses
        );
    }

    public CategoryResponse(Long id, String name, String slug, String description, CategoryStatus status, List<CategoryResponse> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.status = status;
        this.children = children;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public void setStatus(CategoryStatus status) {
        this.status = status;
    }

    public List<CategoryResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryResponse> children) {
        this.children = children;
    }
}