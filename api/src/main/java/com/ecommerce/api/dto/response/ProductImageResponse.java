package com.ecommerce.api.dto.response;

import com.ecommerce.api.entity.ProductImage;

/**
 * DTO response cho ProductImage.
 * KHÔNG expose 'key' (định danh internal trên storage) ra ngoài —
 * client chỉ cần URL để truy cập, không cần biết file thật tên gì.
 */
public class ProductImageResponse {

    private Long id;
    private String url;
    private String originalName;
    private Boolean isPrimary;

    public ProductImageResponse() {}

    public ProductImageResponse(Long id, String url, String originalName, Boolean isPrimary) {
        this.id = id;
        this.url = url;
        this.originalName = originalName;
        this.isPrimary = isPrimary;
    }

    public static ProductImageResponse fromEntity(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getOriginalName(),
                image.getIsPrimary()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
