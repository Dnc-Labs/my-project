package com.ecommerce.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response trả về CHO VNPAY ở endpoint IPN.
 * VNPay yêu cầu đúng format {"RspCode":"..","Message":".."} (viết hoa chữ đầu).
 *
 * @JsonProperty ép tên field JSON = "RspCode"/"Message". Nếu bỏ, Jackson suy ra từ
 * getter getRspCode() → "rspCode" (thường) → VNPay không nhận đúng → cứ retry IPN.
 */
@Getter
@AllArgsConstructor
public class VnpayIpnResponse {

    @JsonProperty("RspCode")
    private String rspCode;

    @JsonProperty("Message")
    private String message;
}
