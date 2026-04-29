package com.ecommerce.api.security;

import com.ecommerce.api.repository.ProductImageRepository;
import com.ecommerce.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Bean check ownership của ProductImage trong @PreAuthorize.
 *
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
     * TODO: Triển khai isOwner — pattern y hệt ProductVariantSecurity:
     * 1. Check authentication != null && isAuthenticated
     * 2. email = authentication.getName()
     * 3. find user → false nếu không có
     * 4. find image → false nếu không có
     * 5. Compare image.getProduct().getSeller().getId() với user.getId()
     */
    public boolean isOwner(Long imageId, Authentication authentication) {
        // TODO: Triển khai
        return false;
    }
}
