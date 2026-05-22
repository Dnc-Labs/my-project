package com.ecommerce.api.dto.response;

import com.ecommerce.api.entity.ProductVariant;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * DTO response cho ProductVariant.
 * Flat productId (không embed nguyên Product object để tránh nặng + circular).
 */
@Getter
@Setter
public class ProductVariantResponse {
    private Long id;
    private String size;
    private String color;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Long productId;
}
