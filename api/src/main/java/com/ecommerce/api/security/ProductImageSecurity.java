package com.ecommerce.api.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ecommerce.api.entity.ProductImage;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.ProductImageRepository;
import com.ecommerce.api.repository.UserRepository;

/**
 * Bean check ownership của ProductImage trong @PreAuthorize.
 * <p>
 * Pattern giống ProductVariantSecurity — image ownership = ownership của Product.
 * Đi qua image.getProduct().getSeller().
 */
@Component("productImageSecurity")
public class ProductImageSecurity {

    private final ProductImageRepository imageRepository;
    private final UserRepository userRepository;

    public ProductImageSecurity(ProductImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check user đang login có phải seller (chủ sở hữu) của product chứa image không.
     * Image ownership = ownership của Product → đi qua image.getProduct().getSeller().
     */
    public boolean isOwner(Long imageId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        String email = authentication.getName();
        Optional<ProductImage> image = imageRepository.findById(imageId);
        Optional<User> user = userRepository.findByEmail(email);
        if (image.isEmpty() || user.isEmpty()) return false;
        return user.get().getId().equals(image.get().getProduct().getSeller().getId());
    }
}
