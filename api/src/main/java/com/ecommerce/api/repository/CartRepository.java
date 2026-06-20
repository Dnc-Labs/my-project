package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);

    // "WithItems" đặt TRƯỚC "By" → Spring Data coi là chữ mô tả, bỏ qua khi parse;
    // điều kiện thực = ByUserId. Nếu để sau By ("findByUserIdWithItems") sẽ bị hiểu
    // nhầm là property → QueryCreationException.
    @EntityGraph(attributePaths = {"cartItems", "cartItems.product", "cartItems.productVariant"})
    Optional<Cart> findWithItemsByUserId(Long userId);
}
