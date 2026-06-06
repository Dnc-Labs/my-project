package com.ecommerce.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Bật Spring Data JPA Auditing cho toàn ứng dụng.
 *
 * @EnableJpaAuditing tự động tìm bean AuditorAware<String> (SpringSecurityAuditorAware)
 * để điền @CreatedBy / @LastModifiedBy. Timestamp (@CreatedDate / @LastModifiedDate)
 * lấy từ DateTimeProvider mặc định (now()).
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
