package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO cho API tạo category.
 * - name (String, bắt buộc)
 * - slug (String, bắt buộc) — URL-friendly, ví dụ: "thoi-trang-nam"
 * - description (String, không bắt buộc)
 * - parentId (Long, không bắt buộc) — nếu null thì là category gốc
 */

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;
    private Long parentId;
}
