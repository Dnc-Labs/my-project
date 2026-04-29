package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * DTO update variant. Tất cả field optional.
 *
 * TODO: Thêm các field:
 * - size (String)
 * - color (String)
 * - sku (String)
 * - price (BigDecimal) → @Positive (không null check vì optional)
 * - stock (Integer) → @PositiveOrZero
 * - imageUrl (String)
 *
 * Lưu ý: KHÔNG có productId — variant đã thuộc 1 product cố định,
 *        không cho phép chuyển variant sang product khác qua API update.
 *        Nếu cần, tạo API riêng "transfer variant".
 *
 * TODO: Tạo getter/setter
 */
public class UpdateProductVariantRequest {
    private String size;
    private String color;
    private String sku;

    @Positive
    private BigDecimal price;

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
