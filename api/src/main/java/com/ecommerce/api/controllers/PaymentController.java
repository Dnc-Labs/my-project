package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.request.CreatePaymentRequest;
import com.ecommerce.api.dto.response.BaseResponse;
import com.ecommerce.api.dto.response.PaymentUrlResponse;
import com.ecommerce.api.dto.response.VnpayIpnResponse;
import com.ecommerce.api.security.CustomUserDetails;
import com.ecommerce.api.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Payment API. userId lấy từ token (@AuthenticationPrincipal), KHÔNG nhận từ client.
 * IP client lấy ở Controller (tầng web) rồi truyền xuống Service — Service không đụng HttpServletRequest.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo URL thanh toán VNPay. Client nhận URL → tự redirect trình duyệt.
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<BaseResponse<PaymentUrlResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(BaseResponse.success(paymentService.createPayment(userDetails.getId(), request.getOrderId(), getClientIp(httpRequest))));
    }

    /**
     * IPN — VNPay gọi server-to-server (không token → public trong SecurityConfig, an toàn nhờ verify chữ ký).
     * Nhận toàn bộ query param vnp_* vào Map. Trả thẳng VnpayIpnResponse (KHÔNG bọc BaseResponse)
     * vì VNPay đòi đúng format {"RspCode","Message"}.
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<VnpayIpnResponse> vnpayIpn(@RequestParam Map<String, String> params) {
        VnpayIpnResponse vnpayIpnResponse =  paymentService.handleIpn(params);
        return ResponseEntity.ok(vnpayIpnResponse);
    }

    /**
     * Return URL — trình duyệt user được redirect về (public). CHỈ hiển thị kết quả cho user.
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<BaseResponse<String>> vnpayReturn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(BaseResponse.success(paymentService.handleReturn(params)));
    }

    /**
     * Lấy IP thật của client. Nếu app đứng sau proxy/load-balancer thì IP thật nằm ở
     * header X-Forwarded-For (dạng "client, proxy1, proxy2") → lấy phần tử đầu.
     * Không có header thì dùng getRemoteAddr() (IP kết nối trực tiếp).
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
