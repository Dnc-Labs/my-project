package com.ecommerce.api.dto.request;

public class OrderRequestDto {
    private String email;

    public OrderRequestDto(String email) {
        this.email = email;
    }
    public OrderRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
