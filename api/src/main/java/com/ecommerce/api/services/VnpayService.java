package com.ecommerce.api.services;

import com.ecommerce.api.config.VnpayProperties;
import com.ecommerce.api.dto.response.VnpayIpnResponse;
import com.ecommerce.api.enums.IpnResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * VnpayService — lo phần giao tiếp với cổng VNPay:
 *  - createPaymentUrl: build URL thanh toán (sort param + ký HMAC-SHA512)
 *  - hmacSha512:       hàm ký lõi (dùng chung cho create + verify)
 *  - verifySignature:  verify chữ ký callback/IPN (dùng ở bước sau)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "payment-gate.type", havingValue = "vnpay", matchIfMissing = true)
public class VnpayService implements PaymentGateway {

    private final VnpayProperties vnpayProperties;
    private static final String ALGORITHM  = "HmacSHA512";
    /**
     * Build URL thanh toán VNPay + ký checksum. Dùng TreeMap để param tự sort theo alphabet:
     * VNPay verify bằng cách sort rồi mới hash → hai bên PHẢI cùng thứ tự thì chữ ký mới khớp.
     */
    public String createPaymentUrl(Long orderId, String txnRef, BigDecimal amount, String ipAddr) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = now.format(formatter);
        String hashSecret = vnpayProperties.getHashSecret();
        TreeMap<String, String > params = new TreeMap<>();
        params.put("vnp_Version", vnpayProperties.getVersion());
        params.put("vnp_Command", vnpayProperties.getCommand());
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());
        // VNPay tính tiền theo đơn vị nhỏ nhất → nhân 100, và phải là số nguyên (không dấu chấm)
        params.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        // KHÔNG dấu tiếng Việt: dấu làm lệch URL-encode giữa 2 bên → sai chữ ký
        params.put("vnp_OrderInfo",  "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnpayProperties.getLocale());
        params.put("vnp_ReturnUrl", vnpayProperties.getReturnUrl());
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", formattedDateTime);

        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            hashData.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }
        String query = hashData.substring(0, hashData.length() - 1);
        String vnp_SecureHash = hmacSha512(hashSecret, query);
        return vnpayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    /**
     * Ký HMAC-SHA512 → chuỗi hex. Checked exception bọc thành IllegalStateException vì
     * thiếu thuật toán/key hỏng là lỗi cấu hình server (5xx), không recover được.
     */
    private String hmacSha512(String key, String data)  {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        }catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Khong the ky HMAC-SHA512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Verify chữ ký VNPay gửi về (return/IPN): ký lại toàn bộ param NHẬN ĐƯỢC rồi so khớp.
     * Ký lại "nhận gì ký nấy" (không phải bộ param mình từng gửi) để luôn khớp dù VNPay thêm
     * field ở chiều về (vnp_TransactionNo, vnp_ResponseCode, vnp_PayDate...).
     */
    public boolean verifySignature(Map<String, String> params) {
        String hashSecret = vnpayProperties.getHashSecret();
        String received = params.get("vnp_SecureHash");
        // Copy trước rồi mới remove (không mutate map của caller); loại chữ ký ra khỏi data cần ký
        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("vnp_SecureHash");
        sortedParams.remove("vnp_SecureHashType");

        StringBuilder stringParams = new StringBuilder();
        for(Map.Entry<String, String> entry : sortedParams.entrySet()) {
            stringParams.append(entry.getKey());
            stringParams.append("=");
            stringParams.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            stringParams.append("&");
        }
        String query = stringParams.substring(0, stringParams.length() - 1);
        String vnp_SecureHash = hmacSha512(hashSecret, query);
        return vnp_SecureHash.equalsIgnoreCase(received);
    }

    /**
     * Dịch param VNPay → PaymentCallback trung lập.
     *
     * Em code: đọc từ params rồi return new PaymentCallback(...):
     *   - txnRef        = params.get("vnp_TxnRef")
     *   - amount        = Long.parseLong(params.get("vnp_Amount"))   (VNPay gửi đã ×100)
     *   - success       = "00".equals(params.get("vnp_ResponseCode"))
     *   - providerTxnNo = params.get("vnp_TransactionNo")
     */
    @Override
    public PaymentCallback parseCallback(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        long amount = Long.parseLong(params.get("vnp_Amount"));
        boolean success = "00".equals(params.get("vnp_ResponseCode"));
        String providerTxnNo = params.get("vnp_TransactionNo");
        return new PaymentCallback(txnRef, amount, success, providerTxnNo);
    }

    /**
     * Map kết quả trung lập → VnpayIpnResponse (RspCode + Message VNPay quy định).
     *
     * Em code (switch trên result):
     *   SUCCESS           -> "00", "Confirm Success"
     *   INVALID_SIGNATURE -> "97", "Invalid Checksum"
     *   ORDER_NOT_FOUND   -> "01", "Order not found"
     *   ALREADY_PROCESSED -> "02", "Order already confirmed"
     *   INVALID_AMOUNT    -> "04", "Invalid Amount"
     */
    @Override
    public Object buildIpnResponse(IpnResult result) {
        return switch (result) {
            case SUCCESS           -> new VnpayIpnResponse("00", "Confirm Success");
            case INVALID_SIGNATURE -> new VnpayIpnResponse("97", "Invalid Checksum");
            case ORDER_NOT_FOUND   -> new VnpayIpnResponse("01", "Order not found");
            case ALREADY_PROCESSED -> new VnpayIpnResponse("02", "Order already confirmed");
            case INVALID_AMOUNT    -> new VnpayIpnResponse("04", "Invalid Amount");
        };
    }
}
