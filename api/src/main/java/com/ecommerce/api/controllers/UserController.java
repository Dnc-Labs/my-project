package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreateUserRequest;
import com.ecommerce.api.dto.request.UpdateUserRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * TODO: Triển khai API đăng ký user
     * - Nhận @Valid @RequestBody CreateUserRequest
     * - Gọi userService.createUser()
     * - Trả về ResponseEntity với status 201 (CREATED) + BaseResponse.success()
     */
    @PostMapping
    public ResponseEntity<BaseResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse userResponse = this.userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(userResponse, "Create Successful"));
    }

    /**
     * TODO: Triển khai API xem profile
     * - Nhận @PathVariable Long id
     * - Gọi userService.getUserById()
     * - Trả về BaseResponse.success()
     */
    @GetMapping("/{id}")
    public BaseResponse<UserResponse> getUser(@PathVariable Long id) {
        UserResponse userResponse = this.userService.getUserById(id);
        return BaseResponse.success(userResponse, "Get Success");
    }

    /**
     * TODO: Triển khai API cập nhật profile
     * - Nhận @PathVariable Long id + @Valid @RequestBody UpdateUserRequest
     * - Gọi userService.updateUser()
     * - Trả về BaseResponse.success()
     */
    @PutMapping("/{id}")
    public BaseResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        UserResponse userResponse = this.userService.updateUser(id, updateUserRequest);
        return BaseResponse.success(userResponse, "Updated Success");
    }
}
