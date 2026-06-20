package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"cartItems", "cartItems.product", "cartItems.productVariant"})
    Optional<Cart> findByUserIdWithItems(Long userId);
}
