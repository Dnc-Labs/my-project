package com.ecommerce.api.config;

import com.ecommerce.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình Spring Security.
 *
 * Bên Express tương đương:
 *   app.use('/api/auth', authRouter);           // public
 *   app.use('/api/users', authMiddleware, ...);  // protected
 *
 * Bên Spring, mọi thứ cấu hình tập trung ở đây.
 */
@Configuration
@EnableWebSecurity       // Bật Spring Security
@EnableMethodSecurity    // Bật @PreAuthorize trên method (phân quyền chi tiết)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Cấu hình SecurityFilterChain — quyết định mọi thứ liên quan đến security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF — không cần cho REST API (CSRF bảo vệ form-based app)
                .csrf(csrf -> csrf.disable())

                // 2. Stateless session — không dùng session/cookie, dùng JWT thay thế
                //    Mỗi request phải tự mang theo token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. Cấu hình endpoint nào public, endpoint nào cần đăng nhập
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập không cần token
                        .requestMatchers("/api/auth/**").permitAll()     // login, register
                        .requestMatchers("/api/users").permitAll()       // tạm thời cho register
                        // Tất cả request còn lại đều cần authentication
                        .anyRequest().authenticated()
                )

                // 4. Thêm JWT filter VÀO TRƯỚC UsernamePasswordAuthenticationFilter
                //    → Request đi qua JWT filter trước, nếu có token hợp lệ thì set authentication
                //    → Nếu không có token, request tiếp tục và bị chặn bởi authorization check
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager — Spring Security dùng để xác thực user.
     * Cần Bean này để dùng trong AuthService khi login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
