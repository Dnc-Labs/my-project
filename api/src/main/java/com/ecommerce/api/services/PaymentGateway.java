package com.ecommerce.api.services;

import com.ecommerce.api.enums.IpnResult;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Contract cho một cổng thanh toán (VNPay, MoMo, PayPal...).
 * Mọi thứ RIÊNG của cổng (ký, đọc param, format ack) nằm sau interface này;
 * PaymentService chỉ làm việc với PaymentGateway → không phụ thuộc cổng cụ thể.
 */
public interface PaymentGateway {

    /** Build URL/redirect để client thanh toán. */
    String createPaymentUrl(Long orderId, String txnRef, BigDecimal amount, String ipAddr);

    /** Verify chữ ký của callback (return/IPN) mà cổng gửi về. */
    boolean verifySignature(Map<String, String> params);

    /** Dịch param riêng của cổng → PaymentCallback trung lập. */
    PaymentCallback parseCallback(Map<String, String> params);

    /** Format ack trả về cho cổng theo kết quả trung lập (VNPay: {RspCode,Message}). */
    Object buildIpnResponse(IpnResult result);
}
