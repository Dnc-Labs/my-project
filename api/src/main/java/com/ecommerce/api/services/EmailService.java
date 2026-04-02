package com.ecommerce.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Async
    public void sendOrderConfirmation(String email, String orderId){
        try {
            logger.info("Bắt đầu gửi email cho {}, đơn hàng {} - Thread: {}", email, orderId, Thread.currentThread().getName());
            Thread.sleep(3000);
            logger.info("Đã gửi email thành công cho {}, đơn hàng {}", email, orderId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
