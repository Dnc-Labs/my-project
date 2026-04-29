package com.ecommerce.api.dto.response;

import com.ecommerce.api.entity.ProductVariant;

import java.math.BigDecimal;

/**
 * DTO response cho ProductVariant.
 * Flat productId (không embed nguyên Product object để tránh nặng + circular).
 *
 * TODO: Thêm các field:
 * - id (Long)
 * - size (String)
 * - color (String)
 * - sku (String)
 * - price (BigDecimal)
 * - stock (Integer)
 * - imageUrl (String)
 * - productId (Long) — chỉ id, không embed
 *
 * TODO: Tạo constructor, getter/setter
 *
 * TODO: Static method:
 *   public static ProductVariantResponse fromEntity(ProductVariant variant) { ... }
 */
public class ProductVariantResponse {
    private Long id;
    private String size;
    private String color;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Long productId;


    public static ProductVariantResponse fromEntity(ProductVariant variant) {
        ProductVariantResponse productVariantResponse = new ProductVariantResponse();
        productVariantResponse.setColor(variant.getColor());
        productVariantResponse.setId(variant.getId());
        productVariantResponse.setSize(variant.getSize());
        productVariantResponse.setSku(variant.getSku());
        productVariantResponse.setPrice(variant.getPrice());
        productVariantResponse.setStock(variant.getStock());
        productVariantResponse.setImageUrl(variant.getImageUrl());
        productVariantResponse.setProductId(variant.getProduct().getId());
        return productVariantResponse;
    }

    public ProductVariantResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
