package com.ecommerce.api.repository;

import com.ecommerce.api.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("""
            SELECT ci FROM CartItem ci
            WHERE ci.cart.id = :cartId
            AND ci.product.id = :productId
            AND (( :variantId IS NULL AND ci.productVariant IS NULL) OR ci.productVariant.id = :variantId)
            """)
    Optional<CartItem> findExisting(@Param("cartId") Long cartId,
                                    @Param("productId") Long productId,
                                    @Param("variantId") Long variantId);

    List<CartItem> findByCartId(Long cartId);
}
