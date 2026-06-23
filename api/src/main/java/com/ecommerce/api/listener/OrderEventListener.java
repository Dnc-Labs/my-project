package com.ecommerce.api.listener;

import com.ecommerce.api.event.OrderCreatedEvent;
import com.ecommerce.api.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Nghe OrderCreatedEvent và kích hoạt gửi mail.
 *
 * Vì sao 2 annotation kết hợp:
 *  - @TransactionalEventListener(phase = AFTER_COMMIT): chỉ chạy SAU KHI transaction checkout COMMIT thành công.
 *    → Nếu checkout rollback, event bị bỏ qua → KHÔNG gửi mail cho đơn không tồn tại.
 *  - @Async("mailExecutor"): chạy trên thread pool mail → không block thread HTTP, SMTP chậm không ảnh hưởng response.
 *
 * Lưu ý self-invocation: listener gọi sang EmailService (bean khác) → qua proxy bình thường, OK.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("========== SEND EMAIL IN THREAD {} ===============", Thread.currentThread().getName());
        emailService.sendOrderConfirmation(event);
    }
}
