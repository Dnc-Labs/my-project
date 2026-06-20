package com.ecommerce.api.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
