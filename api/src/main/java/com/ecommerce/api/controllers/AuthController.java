package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.LoginRequest;
import com.ecommerce.api.dto.request.RefreshTokenRequest;
import com.ecommerce.api.dto.response.AuthResponse;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("login")
    public ResponseEntity<BaseResponse<AuthResponse>> handleLogin(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = this.authService.login(loginRequest);
        return ResponseEntity.ok(BaseResponse.success(authResponse, "Login Success"));
    }

    @PostMapping("refresh")
    public ResponseEntity<BaseResponse<AuthResponse>> handleRefreshToken(@Valid @RequestBody RefreshTokenRequest token) {
        AuthResponse authResponse = this.authService.refreshToken(token.getToken());
        return ResponseEntity.ok(BaseResponse.success(authResponse, "Refresh token success"));
    }

}
