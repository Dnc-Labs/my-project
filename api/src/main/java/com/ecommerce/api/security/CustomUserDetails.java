package com.ecommerce.api.security;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

/**
 * Principal cho JWT auth — bọc thông tin tối thiểu lấy từ token (id, email, role).
 *
 * Tách riêng khỏi entity User để không coupling tầng persistence với security.
 * id dùng cho getReferenceById (gắn quan hệ không cần query).
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String role;   // lưu KHÔNG có prefix "ROLE_" (vd "SELLER")


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    // 4 method trạng thái account — JWT stateless, luôn trả true
    // (nếu cần khoá account thì check ở chỗ khác, không ở đây)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
