package com.ecommerce.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * CartItem — 1 dòng trong giỏ.
 *
 * Merge theo (cart, product, variant): add trùng → cộng dồn quantity, không tạo dòng mới.
 * Unique constraint dưới đây là defense-in-depth; ⚠ MySQL cho phép nhiều dòng NULL nên khi
 * product_variant_id = NULL constraint KHÔNG chặn được → logic merge ở service mới là lớp chính.
 */
@Getter
@Setter
@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cart_product_variant",
        columnNames = {"cart_id", "product_id", "product_variant_id"}
    )
)
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
}
