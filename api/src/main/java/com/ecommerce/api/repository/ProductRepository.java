package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
     boolean existsBySlug(String slug);
     boolean existsBySku(String sku);
     Optional<Product> findBySlug(String slug);
}
