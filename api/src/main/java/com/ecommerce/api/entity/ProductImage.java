package com.ecommerce.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * ProductImage — 1 ảnh thuộc về 1 Product. Mỗi product có nhiều ảnh.
 *
 * - storageKey: định danh trên storage (UUID-based, ví dụ "550e8400-...jpg")
 *   Đặt tên storageKey thay vì key vì "key" là reserved word trong MySQL.
 * - url: URL public để client truy cập ảnh
 * - originalName: tên file gốc client upload (để FE hiển thị/download lại với tên đẹp)
 * - isPrimary: ảnh đại diện. Mỗi product chỉ có 1 ảnh isPrimary=true
 *   (constraint logic ở Service, DB không enforce)
 */
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String storageKey;

    @Column(nullable = false, length = 500)
    private String url;

    private String originalName;

    @Column(nullable = false)
    private Boolean isPrimary = false;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
