package com.ecommerce.api.entity;

import com.ecommerce.api.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer stock = 0;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;

    // Denormalized từ bảng reviews để list/sort/filter theo rating không phải JOIN + AVG mỗi lần đọc.
    // KHÔNG được set 2 field này bằng setter + dirty checking: đó là read-modify-write ở JVM,
    // gây lost update và làm tăng version của Product (review chặn seller sửa sản phẩm).
    // Luôn cập nhật qua ProductRepository.recomputeRating() — 1 câu UPDATE atomic ở DB.
    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> productVariants = new ArrayList<>();

    // @BatchSize: khi list nhiều product, Hibernate gom việc load images của 20 product
    // thành 1 query "WHERE product_id IN (...)" thay vì N query lẻ → chống N+1.
    // KHÔNG dùng JOIN FETCH ở đây vì JOIN collection + Pageable làm sai pagination.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<ProductImage> images = new ArrayList<>();
}
