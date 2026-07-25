package com.ecommerce.api.services;

/**
 * Kết quả callback đã được dịch sang dạng TRUNG LẬP (không còn vnp_* hay momo_*).
 * Mỗi gateway tự đọc param riêng của nó trong parseCallback rồi trả về record này,
 * để PaymentService xử lý nghiệp vụ mà không cần biết cổng nào.
 *
 * @param txnRef        mã giao dịch để tra Payment
 * @param amount        số tiền theo đơn vị nhỏ nhất (đã ×100) — để so khớp với payment.amount×100
 * @param success       giao dịch có thành công không
 * @param providerTxnNo mã giao dịch phía cổng (để đối soát), có thể null
 */
public record PaymentCallback(
        String txnRef,
        long amount,
        boolean success,
        String providerTxnNo
) {}
