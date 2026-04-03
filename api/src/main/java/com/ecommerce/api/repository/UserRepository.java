package com.ecommerce.api.repository;

import com.ecommerce.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository cho User entity.
 *
 * JpaRepository đã cung cấp sẵn: save(), findById(), findAll(), deleteById()...
 *
 * TODO: Thêm custom query method:
 * - Tìm user theo email (dùng cho kiểm tra trùng email khi đăng ký)
 *   Gợi ý: Optional<User> findByEmail(String email)
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
