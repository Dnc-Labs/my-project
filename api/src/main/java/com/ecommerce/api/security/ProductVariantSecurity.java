package com.ecommerce.api.security;

import com.ecommerce.api.entity.ProductVariant;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.ProductVariantRepository;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Bean check ownership của ProductVariant trong @PreAuthorize.
 *
 * Variant ownership = ownership của Product chứa nó.
 * Seller chỉ được sửa/xoá variant của product mình sở hữu.
 *
 * Cách dùng:
 *   @PreAuthorize("hasRole('ADMIN') or
 *                  (hasRole('SELLER') and @productVariantSecurity.isOwner(#variantId, authentication))")
 */
@Component("productVariantSecurity")
@RequiredArgsConstructor
public class ProductVariantSecurity {

    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    /**
     * Check user đang login có phải seller (chủ sở hữu) của product chứa variant không.
     * Đi qua variant.getProduct().getSeller().
     */
    public boolean isOwner(Long variantId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        String email = authentication.getName();
        Optional<User> seller = this.userRepository.findByEmail(email);
        Optional<ProductVariant> variant = this.productVariantRepository.findById(variantId);
        if (seller.isEmpty() || variant.isEmpty()) return false;
        return variant.get().getProduct().getSeller().getId().equals(seller.get().getId());
    }
}
