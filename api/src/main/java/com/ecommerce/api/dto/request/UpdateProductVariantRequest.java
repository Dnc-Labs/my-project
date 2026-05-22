package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO update variant. Tất cả field optional.
 *
 * KHÔNG có productId — variant đã thuộc 1 product cố định, không cho chuyển
 * variant sang product khác qua API update. Nếu cần, tạo API riêng "transfer variant".
 */

@Getter
@Setter
public class UpdateProductVariantRequest {
    private String size;
    private String color;
    private String sku;

    @Positive
    private BigDecimal price;

    @PositiveOrZero
    private Integer stock;
    private String imageUrl;
}
