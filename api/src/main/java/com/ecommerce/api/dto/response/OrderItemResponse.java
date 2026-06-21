package com.ecommerce.api.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Integer quantity;
    private BigDecimal price;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private BigDecimal subTotal;
}
