package com.ecommerce.api.dto.response;

import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.ProductImage;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * DTO response cho Product.
 * Category và seller được flatten (id + name) để tránh infinite loop khi serialize JSON.
 */
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private BigDecimal price;
    private String description;
    private Integer stock = 0;
    private ProductStatus status;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String primaryImageUrl;          // URL ảnh isPrimary, null nếu chưa upload
    private List<ProductImageResponse> images = Collections.emptyList();

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ProductResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public List<ProductImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ProductImageResponse> images) {
        this.images = images;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public static ProductResponse fromEntity(Product product) {
        ProductResponse productResponse = new ProductResponse();
        User seller = product.getSeller();
        Category category = product.getCategory();
        productResponse.setSellerId(seller.getId());
        productResponse.setSellerName(seller.getFullName());
        productResponse.setCategoryId(category.getId());
        productResponse.setCategoryName(category.getName());
        productResponse.setId(product.getId());
        productResponse.setName(product.getName());
        productResponse.setSlug(product.getSlug());
        productResponse.setSku(product.getSku());
        productResponse.setPrice(product.getPrice());
        productResponse.setDescription(product.getDescription());
        productResponse.setStock(product.getStock());
        productResponse.setStatus(product.getStatus());
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());

        List<ProductImage> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            productResponse.setImages(images.stream().map(ProductImageResponse::fromEntity).toList());
            productResponse.setPrimaryImageUrl(
                    images.stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                            .findFirst()
                            .map(ProductImage::getUrl)
                            .orElse(null)
            );
        }
        return productResponse;
    }
}
