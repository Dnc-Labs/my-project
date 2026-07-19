package com.ecommerce.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Trả URL thanh toán VNPay cho client → client redirect trình duyệt sang URL này.
 */
@Getter
@Setter
@AllArgsConstructor
public class PaymentUrlResponse {
    private String paymentUrl;
}
