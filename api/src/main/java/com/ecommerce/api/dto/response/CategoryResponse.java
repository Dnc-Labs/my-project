package com.ecommerce.api.dto.response;
import com.ecommerce.api.enums.CategoryStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * DTO trả về cho client — dạng tree (có chứa children).
 * Mapping bởi MapStruct (CategoryMapper) — tự đệ quy field `children`.
 */

@Getter
@Setter
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private CategoryStatus status;
    private List<CategoryResponse> children;
}
