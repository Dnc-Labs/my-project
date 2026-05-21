package com.ecommerce.api.dto.request;

import com.ecommerce.api.enums.CategoryStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO cho API cập nhật category.
 * - name (String)
 * - slug (String)
 * - description (String)
 * - parentId (Long) — cho phép chuyển category sang parent khác
 * - status (CategoryStatus) — ACTIVE/INACTIVE
 */

@Getter
@Setter
public class UpdateCategoryRequest {
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private CategoryStatus status;
}
