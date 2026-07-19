package com.ecommerce.api.services;

import com.ecommerce.api.dto.response.PaymentUrlResponse;
import com.ecommerce.api.dto.response.VnpayIpnResponse;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.Payment;
import com.ecommerce.api.enums.OrderStatus;
import com.ecommerce.api.enums.PaymentProvider;
import com.ecommerce.api.enums.PaymentStatus;
import com.ecommerce.api.exception.BusinessRuleException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnpayService vnpayService;

    /**
     * Tạo một lần thử thanh toán cho đơn → trả URL VNPay để client redirect.
     * Mỗi lần gọi tạo 1 Payment PENDING mới (append-only) — idempotency thật nằm ở tầng IPN.
     */
    @Transactional
    public PaymentUrlResponse createPayment(Long userId, Long orderId, String ipAddr) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        // Không khớp chủ đơn → 404 (không phải 403) để không lộ sự tồn tại đơn của người khác
        if(!order.getCustomer().getId().equals(userId)) throw new ResourceNotFoundException("The order is not belong to user");
        if(!order.getStatus().equals(OrderStatus.PENDING)) throw new BusinessRuleException("The order is not pending");
        String txnRef = genTxnRef();
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setProvider(PaymentProvider.VNPAY);
        payment.setTxnRef(txnRef);
        // new Payment() là transient → phải save() tường minh (dirty checking chỉ áp cho entity managed)
        paymentRepository.save(payment);
        String url = vnpayService.createPaymentUrl(orderId, txnRef, order.getTotalAmount(), ipAddr);
        return new PaymentUrlResponse(url);
    }

    /**
     * Xử lý IPN (server-to-server) — SOURCE OF TRUTH cập nhật trạng thái tiền + đơn.
     * VNPay có thể gọi NHIỀU LẦN (retry khi không nhận đủ RspCode) → phải idempotent.
     * RspCode trả về là "biên nhận": "00" = đã nhận & xử lý (kể cả khi giao dịch FAILED),
     * để VNPay ngừng retry — KHÔNG phải nghĩa "giao dịch thành công".
     */
    @Transactional
    public VnpayIpnResponse handleIpn(Map<String, String> params) {
        if(!vnpayService.verifySignature(params)) return new VnpayIpnResponse("97", "Invalid Checksum");
        String txnRef = params.get("vnp_TxnRef");
        Optional<Payment> paymentOptional = paymentRepository.findByTxnRef(txnRef);
        if(paymentOptional.isEmpty()) return new VnpayIpnResponse("01", "Order not found");
        Payment payment = paymentOptional.get();
        // Idempotency: đã xử lý (status != PENDING) → bỏ qua, không cập nhật lại
        if(!payment.getStatus().equals(PaymentStatus.PENDING)) return new VnpayIpnResponse("02", "Order already confirmed");
        // vnp_Amount VNPay gửi đã ×100; so bằng long để tránh bẫy scale của BigDecimal.equals
        Long vnpAmount = new BigDecimal(params.get("vnp_Amount")).longValue();
        if(!vnpAmount.equals(payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()))  return new VnpayIpnResponse("04", "Invalid Amount");
        payment.setRawResponse(params.toString());
        payment.setTransactionNo(params.get("vnp_TransactionNo"));
        Order order = payment.getOrder();

        if("00".equals(params.get("vnp_ResponseCode"))) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CONFIRMED);
        }
        else
            payment.setStatus(PaymentStatus.FAILED);

        // payment/order là managed → dirty checking tự flush khi commit, không cần save()
        return new VnpayIpnResponse("00", "Confirm Success");
    }

    /**
     * Xử lý Return URL (trình duyệt user) — CHỈ verify chữ ký + đọc kết quả hiển thị UI.
     * TUYỆT ĐỐI không update DB: user có thể không bao giờ về tới đây (tắt tab) → IPN mới là nguồn cập nhật.
     */
    public String handleReturn(Map<String, String> params) {
        if(!vnpayService.verifySignature(params)) throw new BusinessRuleException("Invalid signature");
        return "00".equals(params.get("vnp_ResponseCode")) ? "Thanh toan thanh cong" : "Thanh toan that bai";
    }

    /**
     * Sinh mã giao dịch duy nhất: timestamp (đối soát theo thời gian) + random (tránh trùng cùng giây).
     * Không dùng id Payment vì id tuần tự dễ đoán và phải chạm DB trước để có id.
     */
    private String genTxnRef() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
