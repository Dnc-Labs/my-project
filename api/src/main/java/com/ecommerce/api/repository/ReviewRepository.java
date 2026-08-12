package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Review;
import com.ecommerce.api.enums.ReviewStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {


    // @EntityGraph ở cả 3 query: response cần user.fullName và product.id, mà 2 quan hệ này khai
    // LAZY. Thiếu nó thì mỗi review phát sinh query lẻ để nạp proxy → N+1.
    // An toàn khi đi kèm Pageable vì cả hai đều là quan hệ to-one (JOIN không nhân dòng).
    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    // Cố ý KHÔNG lọc status: review bị ADMIN gỡ thì user mất luôn quyền viết lại sản phẩm đó.
    // Nếu lọc ACTIVE, user sẽ INSERT lại rồi ăn 409 từ unique constraint — lỗi vô nghĩa với họ.
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findByUserIdAndStatus(Long userId, ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "product"})
    Optional<Review> findWithProductById(Long id);

}
