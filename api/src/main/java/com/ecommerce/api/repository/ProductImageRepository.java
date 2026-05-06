package com.ecommerce.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.api.entity.ProductImage;

/**
 * Repository cho ProductImage.
 * findByProductIdAndIsPrimaryTrue — dùng khi set primary mới (cần update
 * isPrimary=false cho ảnh primary cũ).
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    long countByProductId(Long productId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);
}
