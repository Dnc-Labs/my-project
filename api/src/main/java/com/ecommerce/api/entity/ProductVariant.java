package com.ecommerce.api.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * ProductVariant — biến thể của Product (kết hợp size + color).
 * Composite unique constraint (product_id, size, color) đảm bảo trong 1 product
 * không có 2 variant trùng nhau về size + color.
 */
@Getter
@Setter
@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_variant_product_size_color",
        columnNames = {"product_id", "size", "color"}
    )
)
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String color;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
