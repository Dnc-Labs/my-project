package com.ecommerce.api.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
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
}
