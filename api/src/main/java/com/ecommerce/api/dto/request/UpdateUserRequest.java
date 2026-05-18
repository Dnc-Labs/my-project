package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO cho API cập nhật profile.
 * Không cho phép đổi email và password ở đây (sẽ có API riêng sau).
 */

@Getter
@Setter
public class UpdateUserRequest {
    @NotBlank
    private String fullName;
    private String phone;
    private String address;
}
