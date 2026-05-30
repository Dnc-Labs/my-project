package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
