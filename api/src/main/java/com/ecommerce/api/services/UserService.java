package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.CreateUserRequest;
import com.ecommerce.api.dto.request.UpdateUserRequest;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * TODO: Triển khai tạo user
     * 1. Kiểm tra email đã tồn tại chưa → nếu rồi thì throw exception
     * 2. Tạo User entity từ request
     * 3. Set createdAt = LocalDateTime.now()
     * 4. Lưu vào database
     * 5. Trả về UserResponse
     */
    public UserResponse createUser(CreateUserRequest request) {
        Optional<User> existUser = userRepository.findByEmail(request.getEmail());
        if(existUser.isPresent()) throw new RuntimeException("Email is already registered");
        String passwordHash = this.passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordHash);
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCreatedAt(LocalDateTime.now());
        User createdUser = this.userRepository.save(user);
        return UserResponse.fromEntity(createdUser);
    }

    /**
     * TODO: Triển khai lấy user theo id
     * 1. Tìm user theo id → nếu không có thì throw ResourceNotFoundException
     * 2. Trả về UserResponse
     */
    public UserResponse getUserById(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    /**
     * TODO: Triển khai cập nhật user
     * 1. Tìm user theo id → nếu không có thì throw ResourceNotFoundException
     * 2. Cập nhật các field từ request
     * 3. Set updatedAt = LocalDateTime.now()
     * 4. Lưu vào database
     * 5. Trả về UserResponse
     */
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.getFullName() != null && !request.getFullName().isBlank())
            user.setFullName(request.getFullName());
        if (request.getAddress() != null && !request.getAddress().isBlank())
            user.setAddress(request.getAddress());
        if (request.getPhone() != null && !request.getPhone().isBlank())
            user.setPhone(request.getPhone());
        user.setUpdatedAt(LocalDateTime.now());
        User userUpdate = this.userRepository.save(user);
        return UserResponse.fromEntity(userUpdate);
    }
}
