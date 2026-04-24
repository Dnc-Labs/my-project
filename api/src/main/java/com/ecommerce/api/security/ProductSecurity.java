package com.ecommerce.api.security;

import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Bean dùng để check ownership của Product trong @PreAuthorize.
 *
 * Cách dùng trong controller:
 *   @PreAuthorize("hasRole('ADMIN') or @productSecurity.isOwner(#id, authentication)")
 *
 * Vì sao đặt trong package security chứ không phải services?
 * - Đây là logic BẢO MẬT (authorization), không phải business logic
 * - Tách riêng để dễ test, dễ tái sử dụng cho các resource khác
 *
 * Vì sao dùng @Component("productSecurity") (có tên)?
 * - SpEL gọi bằng @beanName → cần đặt tên cho bean
 * - Nếu không đặt tên, Spring mặc định lấy tên class viết thường ký tự đầu
 *   → vẫn gọi được bằng @productSecurity, nhưng khai báo rõ tên thì an toàn
 *   khi đổi tên class sau này (refactor an toàn hơn)
 */
@Component("productSecurity")
public class ProductSecurity {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductSecurity(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Kiểm tra user đang login có phải là seller (chủ sở hữu) của product không.
     *
     * Lưu ý: Method này được gọi TRƯỚC khi method controller chạy
     *        → query DB thêm 2 lần (user + product). Chấp nhận được vì
     *        ownership check là thao tác quan trọng, không nên cache.
     */
    public boolean isOwner(Long productId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) return false;
        String email = authentication.getName();
        Optional<User> seller = this.userRepository.findByEmail(email);
        Optional<Product> product = this.productRepository.findById(productId);
        if(seller.isEmpty() || product.isEmpty()) return false;
        return product.get().getSeller().getId().equals(seller.get().getId());
    }
}
