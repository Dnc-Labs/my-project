package com.ecommerce.api.dto.request;

import com.ecommerce.api.enums.ProductStatus;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Gom các tham số filter cho endpoint list product.
 * Spring tự bind query param (?keyword=x&categoryId=5&minPrice=...) vào field cùng tên
 * (cơ chế @ModelAttribute ngầm cho object non-body).
 * <p>
 * Mọi field optional (có thể null) — null nghĩa là "không filter theo tiêu chí này".
 */
@Data
public class ProductFilterRequest {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductStatus status;
}
