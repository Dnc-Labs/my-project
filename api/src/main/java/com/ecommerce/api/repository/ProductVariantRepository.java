package com.ecommerce.api.repository;

import com.ecommerce.api.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ProductVariant.
 *
 * TODO: Thêm các custom query method:
 * - existsBySku(String sku) → check trùng SKU khi tạo
 * - existsByProductIdAndSizeAndColor(Long productId, String size, String color)
 *   → check trùng combination size+color trong cùng 1 product (DB cũng có
 *     unique constraint, nhưng check trước để trả lỗi rõ ràng hơn)
 * - findBySku(String sku) → lookup theo SKU (dùng khi scan barcode)
 * - List<ProductVariant> findByProductId(Long productId) → list variants của product
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);
    boolean existsByProductIdAndSizeAndColor(Long productId, String size, String color);
    Optional<ProductVariant> findBySku(String sku);
    List<ProductVariant> findByProductId(Long productId);
}
