package com.ecommerce.api.dto.response;

import com.ecommerce.api.entity.User;
import com.ecommerce.api.enums.Role;
import com.ecommerce.api.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Role role;
    private Status status;
}
