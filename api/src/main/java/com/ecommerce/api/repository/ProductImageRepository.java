package com.ecommerce.api.repository;

import com.ecommerce.api.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho ProductImage.
 *
 * TODO: Thêm các custom query method:
 * - List<ProductImage> findByProductId(Long productId)
 *   → list tất cả ảnh của 1 product
 * - long countByProductId(Long productId)
 *   → đếm để enforce limit (max 8 ảnh per product chẳng hạn)
 * - Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId)
 *   → tìm ảnh primary hiện tại (dùng khi set primary mới phải update cái cũ)
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
