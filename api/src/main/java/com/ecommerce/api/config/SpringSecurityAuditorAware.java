package com.ecommerce.api.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cầu nối giữa Spring Data Auditing và Spring Security.
 *
 * AuditingEntityListener gọi getCurrentAuditor() mỗi khi cần điền
 * @CreatedBy / @LastModifiedBy. Đây là nơi DUY NHẤT đọc SecurityContext —
 * entity hoàn toàn không biết gì về Security.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM = "SYSTEM";

    @Override
    public Optional<String> getCurrentAuditor() {
        // Fallback "SYSTEM" cho request chưa login (seed, public endpoint) hoặc
        // background job — KHÔNG trả Optional.empty() vì sẽ để @CreatedBy null, mất audit trail.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            return Optional.of(SYSTEM);
        }
        return Optional.of(authentication.getName());
    }
}
