package com.ecommerce.api.repository;

import com.ecommerce.api.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ProductVariant.
 * existsByProductIdAndSizeAndColor — DB có unique constraint nhưng check
 * trước ở Service để trả lỗi rõ ràng (thay vì SQLIntegrityConstraintViolation).
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);
    boolean existsByProductIdAndSizeAndColor(Long productId, String size, String color);
    Optional<ProductVariant> findBySku(String sku);
    List<ProductVariant> findByProductId(Long productId);
}
