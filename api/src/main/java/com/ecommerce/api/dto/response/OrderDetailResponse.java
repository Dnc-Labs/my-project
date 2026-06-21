package com.ecommerce.api.dto.response;

import com.ecommerce.api.enums.OrderStatus;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailResponse {
    private Long id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private String note;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
