package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Product;
import com.ecommerce.api.enums.ProductStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Product entity.
 * <p>
 * JpaSpecificationExecutor cung cấp findAll(Specification, Pageable) — nền tảng
 * cho filter động ở 3.3.3 (thay cho @Query search tĩnh của 3.3.2).
 */
@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {
     boolean existsBySlug(String slug);
     boolean existsBySku(String sku);
     Optional<Product> findBySlug(String slug);
     Optional<Product> findByIdAndStatus(Long productId, ProductStatus status);
     boolean existsByIdAndStatus(Long productId, ProductStatus status);

     /**
      * Tính lại average_rating + review_count từ bảng reviews.
      * <p>
      * Đẩy phép tính XUỐNG DB thay vì load Product rồi setter: đọc-tính-ghi nằm gọn trong 1
      * statement nên không có khoảng hở cho lost update, và không làm tăng Product.version
      * (nếu tăng, SELLER đang sửa sản phẩm sẽ ăn 409 oan chỉ vì có người review).
      * <p>
      * flushAutomatically: BẮT BUỘC — ép flush INSERT/UPDATE review xuống DB trước, nếu không
      * AVG chạy trên bảng chưa có review vừa lưu → mọi review trễ một nhịp.
      * clearAutomatically: query đi thẳng xuống DB, vượt qua persistence context → entity Product
      * đang giữ trong context sẽ stale (kể cả findById cũng trả bản cũ từ first-level cache).
      * COALESCE: AVG trên tập rỗng trả NULL — xảy ra khi ADMIN gỡ review cuối cùng.
      */
     @Modifying(flushAutomatically = true, clearAutomatically = true)
     @Query(value = """
             UPDATE products p
             SET
              average_rating = (SELECT COALESCE(AVG(r.rating), 0) FROM reviews r WHERE r.product_id = :productId AND r.status = 'ACTIVE'),
              review_count = (SELECT COUNT(*) FROM reviews r WHERE r.product_id = :productId AND r.status = 'ACTIVE')
             WHERE p.id = :productId
             """, nativeQuery = true)
     void recomputeRating(@Param("productId") Long productId);
}
