package com.ecommerce.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter xử lý JWT authentication.
 *
 * Tương đương middleware bên Express:
 *   const authMiddleware = (req, res, next) => {
 *       const token = req.headers.authorization?.split(' ')[1];
 *       const decoded = jwt.verify(token, secret);
 *       req.user = decoded;
 *       next();
 *   };
 *
 * Extends OncePerRequestFilter → đảm bảo chỉ chạy 1 lần mỗi request
 * (trong Spring, một request có thể đi qua filter nhiều lần nếu có forward/redirect)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy token từ header "Authorization: Bearer <token>"
        String token = extractToken(request);

        // 2. Nếu có token và token hợp lệ → set authentication vào SecurityContext
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Tạo Authentication object
            // Tương đương req.user = { email, role } bên Express
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,                                          // principal (ai đang đăng nhập)
                            null,                                           // credentials (không cần password ở đây)
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))  // authorities (quyền)
                    );

            // Đặt vào SecurityContext — từ đây các layer sau đều biết user là ai
            // Giống req.user trong Express, nhưng là thread-safe (mỗi thread có context riêng)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. Tiếp tục filter chain (giống next() trong Express middleware)
        filterChain.doFilter(request, response);
    }

    /**
     * Trích xuất token từ header Authorization
     * Format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // bỏ "Bearer " lấy phần token
        }
        return null;
    }
}
