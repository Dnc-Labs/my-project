package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO tạo variant cho 1 product.
 * KHÔNG có productId — productId lấy từ path variable của URL
 * (POST /api/products/{productId}/variants).
 */
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
