package com.ecommerce.api.entity;

import com.ecommerce.api.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Review — đánh giá sản phẩm của khách đã mua (đơn DELIVERED).
 * <p>
 * uniqueConstraints khai ở đây CHỈ có tác dụng khi Hibernate tự sinh schema.
 * Dự án dùng ddl-auto:validate + Flyway → ràng buộc thật nằm ở V3__create_reviews.sql.
 * Vẫn khai để đọc entity là thấy luật, và để test dùng H2 create-drop vẫn đúng.
 */
@Getter
@Setter
@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(name = "uk_review_user_product",
                                             columnNames = {"user_id", "product_id"}))
public class Review extends BaseEntity {

    // Cố ý KHÔNG thêm @OneToMany List<Review> vào Product:
    // 1 sản phẩm có thể có hàng nghìn review, để Hibernate quản lý collection đó là mời
    // gọi OutOfMemory + N+1. Mọi truy cập review đi qua ReviewRepository + Pageable.
    // Quan hệ ở đây là 1 chiều (Review biết Product, Product không biết Review).

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
