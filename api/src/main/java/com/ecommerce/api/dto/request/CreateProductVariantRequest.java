package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO tạo variant cho 1 product.
 * KHÔNG có productId — productId lấy từ path variable của URL
 * (POST /api/products/{productId}/variants).
 */

@Getter
@Setter
public class CreateProductVariantRequest {
    @NotBlank
    private String size;

    @NotBlank
    private String color;

    @NotBlank
    private String sku;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    private String imageUrl;

}
