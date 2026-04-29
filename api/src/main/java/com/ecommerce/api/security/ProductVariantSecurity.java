package com.ecommerce.api.security;

import com.ecommerce.api.entity.ProductVariant;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.ProductVariantRepository;
import com.ecommerce.api.repository.UserRepository;
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
public class ProductVariantSecurity {

    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public ProductVariantSecurity(ProductVariantRepository productVariantRepository,
                                   UserRepository userRepository) {
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    /**
     * TODO: Triển khai isOwner cho variant
     * 1. Check authentication != null và đã authenticated
     * 2. Lấy email từ authentication.getName()
     * 3. Tìm user theo email → false nếu không có
     * 4. Tìm variant theo variantId → false nếu không có
     * 5. So sánh variant.getProduct().getSeller().getId() với user.getId()
     *
     * Pattern giống ProductSecurity.isOwner — chỉ khác ở bước 5
     * (đi qua getProduct() rồi mới đến seller).
     */
    public boolean isOwner(Long variantId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) return false;
        String currentUserEmail =  authentication.getName();
        Optional<User> currentUserObject = this.userRepository.findByEmail(currentUserEmail);
        if(currentUserObject.isEmpty()) return false;
        User seller = currentUserObject.get();
        Optional<ProductVariant> productVariantObject = this.productVariantRepository.findById(variantId);
        if(productVariantObject.isEmpty()) return false;
        ProductVariant productVariant = productVariantObject.get();
        return seller.getId().equals(productVariant.getProduct().getSeller().getId());
    }
}
