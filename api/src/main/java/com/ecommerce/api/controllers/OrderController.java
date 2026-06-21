package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreateOrderRequest;
import com.ecommerce.api.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.OrderDetailResponse;
import com.ecommerce.api.dto.response.OrderSummaryResponse;
import com.ecommerce.api.dto.response.PageResponse;
import com.ecommerce.api.enums.OrderStatus;
import com.ecommerce.api.security.CustomUserDetails;
import com.ecommerce.api.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Order API. userId/role lấy từ token (@AuthenticationPrincipal), KHÔNG nhận từ client.
 * Mọi endpoint yêu cầu đăng nhập (anyRequest().authenticated() ở SecurityConfig).
 * Phân quyền đổi status xử lý trong service (checkPermission), không ở @PreAuthorize
 * vì luật phụ thuộc cả role LẪN chủ đơn (customer cancel / staff advance).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Đặt hàng từ giỏ → 201 Created (tạo resource mới)
    @PostMapping
    public ResponseEntity<BaseResponse<OrderDetailResponse>> checkout(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderDetailResponse res = orderService.checkout(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(res));
    }

    // Danh sách đơn của tôi (summary, phân trang, lọc status optional)
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
         PageResponse<OrderSummaryResponse> orders =  orderService.getMyOrders(userDetails.getId(), status, pageable);
         return ResponseEntity.ok(BaseResponse.success(orders));
    }

    // Chi tiết 1 đơn (kèm items) — ownership check trong service
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(BaseResponse.success(orderService.getOrderDetail(userDetails.getId(), id)));
    }

    // Đổi trạng thái (confirm/ship/deliver/cancel) — state machine + phân quyền trong service
    @PutMapping("/{id}/status")
    public ResponseEntity<BaseResponse<OrderDetailResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderDetailResponse order =  orderService.updateStatus(userDetails.getId(), userDetails.getRole(), id, request.getStatus());
        return ResponseEntity.ok(BaseResponse.success(order));
    }
}
