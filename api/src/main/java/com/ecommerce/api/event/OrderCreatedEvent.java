package com.ecommerce.api.event;

import java.math.BigDecimal;
import java.util.List;

/**
 * Snapshot dữ liệu đơn hàng tại thời điểm checkout, để gửi email ở thread khác.
 *
 * Vì sao là record (bất biến) và KHÔNG truyền Order entity?
 *  - Thread async không có Hibernate Session → chạm field lazy (customer, orderItems...) sẽ
 *    ném LazyInitializationException. Nên phải "đóng băng" data trong thread checkout (session còn mở)
 *    rồi truyền snapshot này sang.
 */
public record OrderCreatedEvent(
        String customerEmail,
        String customerName,
        Long orderId,
        BigDecimal totalAmount,
        List<Line> items
) {
    public record Line(String productName, String variantName, BigDecimal price, int quantity) {}
}
