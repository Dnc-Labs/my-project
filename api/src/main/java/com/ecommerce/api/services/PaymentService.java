package com.ecommerce.api.services;

import com.ecommerce.api.dto.response.PaymentUrlResponse;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.Payment;
import com.ecommerce.api.enums.IpnResult;
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
    private final PaymentGateway paymentGateway;

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
        String url = paymentGateway.createPaymentUrl(orderId, txnRef, order.getTotalAmount(), ipAddr);
        return new PaymentUrlResponse(url);
    }

    /**
     * Xử lý IPN (server-to-server) — SOURCE OF TRUTH cập nhật trạng thái tiền + đơn.
     * VNPay có thể gọi NHIỀU LẦN (retry khi không nhận đủ RspCode) → phải idempotent.
     * RspCode trả về là "biên nhận": "00" = đã nhận & xử lý (kể cả khi giao dịch FAILED),
     * để VNPay ngừng retry — KHÔNG phải nghĩa "giao dịch thành công".
     */
    @Transactional
    public Object handleIpn(Map<String, String> params) {
        if(!paymentGateway.verifySignature(params)) return paymentGateway.buildIpnResponse(IpnResult.INVALID_SIGNATURE);
        PaymentCallback cb = paymentGateway.parseCallback(params);
        Optional<Payment> paymentOptional = paymentRepository.findByTxnRef(cb.txnRef());
        if(paymentOptional.isEmpty()) return paymentGateway.buildIpnResponse(IpnResult.ORDER_NOT_FOUND);
        Payment payment = paymentOptional.get();
        // Idempotency: đã xử lý (status != PENDING) → bỏ qua, không cập nhật lại
        if(!payment.getStatus().equals(PaymentStatus.PENDING)) return paymentGateway.buildIpnResponse(IpnResult.ALREADY_PROCESSED);
        // amount trong callback đã ×100; so bằng long để tránh bẫy scale của BigDecimal.equals
        long expected = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if(cb.amount() != expected) return paymentGateway.buildIpnResponse(IpnResult.INVALID_AMOUNT);
        payment.setRawResponse(params.toString());
        payment.setTransactionNo(cb.providerTxnNo());
        Order order = payment.getOrder();

        if(cb.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CONFIRMED);
        }
        else
            payment.setStatus(PaymentStatus.FAILED);

        // payment/order là managed → dirty checking tự flush khi commit, không cần save()
        return paymentGateway.buildIpnResponse(IpnResult.SUCCESS);
    }

    /**
     * Xử lý Return URL (trình duyệt user) — CHỈ verify chữ ký + đọc kết quả hiển thị UI.
     * TUYỆT ĐỐI không update DB: user có thể không bao giờ về tới đây (tắt tab) → IPN mới là nguồn cập nhật.
     */
    public String handleReturn(Map<String, String> params) {
        if(!paymentGateway.verifySignature(params)) throw new BusinessRuleException("Invalid signature");
        return paymentGateway.parseCallback(params).success() ? "Thanh toan thanh cong" : "Thanh toan that bai";
    }

    /**
     * Sinh mã giao dịch duy nhất: timestamp (đối soát theo thời gian) + random (tránh trùng cùng giây).
     * Không dùng id Payment vì id tuần tự dễ đoán và phải chạm DB trước để có id.
     */
    private String genTxnRef() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
