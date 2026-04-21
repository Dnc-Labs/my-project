package com.ecommerce.api.dto.request;

import com.ecommerce.api.enums.CategoryStatus;

/**
 * DTO cho API cập nhật category.
 * - name (String)
 * - slug (String)
 * - description (String)
 * - parentId (Long) — cho phép chuyển category sang parent khác
 * - status (CategoryStatus) — ACTIVE/INACTIVE
 */

public class UpdateCategoryRequest {
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private CategoryStatus status;

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public void setStatus(CategoryStatus status) {
        this.status = status;
    }
}