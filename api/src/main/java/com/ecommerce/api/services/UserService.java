package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateUserRequest;
import com.ecommerce.api.dto.request.UpdateUserRequest;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.DuplicateResource;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.mapper.UserMapper;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        boolean existUser = userRepository.existsByEmail(request.getEmail());
        if (existUser) throw new DuplicateResource("Email is already registered");
        User user = userMapper.fromRequestDto(request);
        // hash password sau khi map vì mapper cố ý ignore field password
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        User createdUser = this.userRepository.save(user);
        return userMapper.fromEntity(createdUser);
    }

    public UserResponse getUserById(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userMapper.updateEntity(request, user);
        User userUpdate = this.userRepository.save(user);
        return userMapper.fromEntity(userUpdate);
    }
}
