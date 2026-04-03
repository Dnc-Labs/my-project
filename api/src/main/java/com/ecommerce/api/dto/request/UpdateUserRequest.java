package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO cho API cập nhật profile.
 * Không cho phép đổi email và password ở đây (sẽ có API riêng sau).
 *
 * TODO: Thêm các field + validation:
 * - fullName (String) → @NotBlank
 * - phone (String) → không bắt buộc
 * - address (String) → không bắt buộc
 *
 * TODO: Tạo getter/setter cho tất cả field
 */
public class UpdateUserRequest {

}
