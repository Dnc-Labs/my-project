package com.ecommerce.api.services;

import com.ecommerce.api.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Đóng gói việc gửi email. JavaMailSender được Spring Boot autoconfigure từ spring.mail.* trong yaml.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    /**
     * Gửi email xác nhận đơn hàng. Nuốt mọi exception (chỉ log) — gửi mail fail không được
     * làm ảnh hưởng tới đơn hàng đã tạo thành công.
     */
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(event.customerEmail());
        mailMessage.setSubject("Xác nhận đơn hàng #" + event.orderId());
        mailMessage.setText(buildBody(event));

        try {
            mailSender.send(mailMessage);
        } catch (Exception e) {
            log.error("Gửi email xác nhận đơn #{} thất bại", event.orderId(), e);
        }
    }

    private String buildBody(OrderCreatedEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào ").append(event.customerName()).append(",\n\n");
        sb.append("Cảm ơn bạn đã đặt hàng. Đơn #").append(event.orderId()).append(" đã được tạo thành công.\n\n");
        sb.append("Chi tiết đơn hàng:\n");
        for (OrderCreatedEvent.Line line : event.items()) {
            sb.append("  - ").append(line.productName());
            if (line.variantName() != null) sb.append(" (").append(line.variantName()).append(")");
            sb.append(" x").append(line.quantity())
              .append(" - ").append(line.price()).append("\n");
        }
        sb.append("\nTổng tiền: ").append(event.totalAmount()).append("\n\n");
        sb.append("Trân trọng.");
        return sb.toString();
    }
}
