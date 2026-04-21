package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO cho API tạo category.
 * - name (String, bắt buộc)
 * - slug (String, bắt buộc) — URL-friendly, ví dụ: "thoi-trang-nam"
 * - description (String, không bắt buộc)
 * - parentId (Long, không bắt buộc) — nếu null thì là category gốc
 */
public class CreateCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;
    private Long parentId;

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
}