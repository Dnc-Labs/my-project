package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.LoginRequest;
import com.ecommerce.api.dto.response.AuthResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.InvalidToken;
import com.ecommerce.api.exception.InvalidUserOrPassword;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest loginRequest){
        String userEmail = loginRequest.getEmail();
        User user = this.userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User is not found"));
        boolean checkValidPassword = this.passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if(!checkValidPassword) throw new InvalidUserOrPassword("Username or password is incorrect");

        String accessToken = this.jwtTokenProvider.generateAccessToken(loginRequest.getEmail(), user.getRole().toString());
        String refreshToken = this.jwtTokenProvider.generateRefreshToken(loginRequest.getEmail());
        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        boolean validToken = this.jwtTokenProvider.validateToken(refreshToken);
        if(!validToken) throw new InvalidToken();
        String email = this.jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User is not found"));
        String accessToken = this.jwtTokenProvider.generateAccessToken(email, user.getRole().toString());
        return new AuthResponse(accessToken, refreshToken);
    }
}
