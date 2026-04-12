package com.ecommerce.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Class chịu trách nhiệm tạo và verify JWT token.
 * Giống jwt.sign() / jwt.verify() bên Node.js.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        // Tạo SecretKey từ string — dùng cho thuật toán HMAC-SHA256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Tạo Access Token
     * Payload chứa: subject (email), role, thời gian tạo, thời gian hết hạn
     */
    public String generateAccessToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(email)                    // "sub" — ai là chủ token
                .claim("role", role)               // custom claim — quyền của user
                .issuedAt(now)                     // "iat" — tạo lúc nào
                .expiration(expiry)                // "exp" — hết hạn lúc nào
                .signWith(secretKey)               // ký bằng secret key
                .compact();                        // build thành string
    }

    /**
     * Tạo Refresh Token
     * Payload đơn giản hơn, chỉ cần email + thời hạn dài hơn
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Lấy email (subject) từ token
     * Giống jwt.verify(token, secret).sub bên Node.js
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Lấy role từ token
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Kiểm tra token có hợp lệ không
     * - Chữ ký đúng?
     * - Chưa hết hạn?
     * - Format đúng?
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token không hợp lệ: sai chữ ký, hết hạn, sai format...
            return false;
        }
    }

    /**
     * Parse và verify token → trả về Claims (payload)
     * Nếu token không hợp lệ → throw JwtException
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)       // set key để verify chữ ký
                .build()
                .parseSignedClaims(token)    // parse + verify
                .getPayload();               // lấy payload (claims)
    }
}
