package com.ecommerce.api.enums;

/**
 * Kết quả xử lý IPN — TRUNG LẬP, không dính cổng nào.
 * PaymentService quyết định kết quả theo enum này; mỗi gateway tự map sang format ack của nó
 * (VNPay: RspCode "00"/"97"/... ; MoMo: HTTP 204/... ) trong buildIpnResponse.
 */
public enum IpnResult {
    SUCCESS,            // đã nhận & xử lý IPN (kể cả khi giao dịch FAILED)
    INVALID_SIGNATURE,  // chữ ký sai
    ORDER_NOT_FOUND,    // không tìm thấy Payment theo txnRef
    ALREADY_PROCESSED,  // idempotency: đã xử lý rồi
    INVALID_AMOUNT      // số tiền không khớp
}
