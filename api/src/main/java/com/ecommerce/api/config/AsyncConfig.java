package com.ecommerce.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Bật cơ chế @Async toàn app + khai báo thread pool RIÊNG cho việc gửi mail.
 *
 * Vì sao pool riêng (không xài SimpleAsyncTaskExecutor mặc định)?
 *  - Mặc định Spring tạo 1 thread MỚI cho mỗi lần gọi @Async → không giới hạn → spam SMTP có thể nổ thread.
 *  - Pool riêng kiểm soát được số thread đồng thời + có hàng đợi.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Tên bean "mailExecutor" được tham chiếu ở @Async("mailExecutor").
     * Email không gấp → pool nhỏ (core 2 / max 4) + queue 50 là đủ.
     */
    @Bean("mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.initialize();
        return executor;
    }
}
