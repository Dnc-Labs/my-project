package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CreateOrderRequest {
    @NotBlank
    private String shippingAddress;
    private String note;
}
