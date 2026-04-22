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
    @NotBlank
    private String fullName;

    private String phone;
    private String address;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
