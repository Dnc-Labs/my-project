package com.ecommerce.api.config;

import com.ecommerce.api.dto.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Xử lý 403 Forbidden cho authorization fail ở tầng FILTER (URL-level,
 * vd .requestMatchers(...).hasRole("ADMIN")).
 *
 * Đối xứng với JwtAuthenticationEntryPoint (xử lý 401). Cả 2 là callback cho
 * ExceptionTranslationFilter — nơi @RestControllerAdvice không với tới được.
 *
 * Lưu ý: 403 từ @PreAuthorize (method-level) đi qua DispatcherServlet nên đã
 * được GlobalExceptionHandler xử lý — handler này chỉ cho URL-level authz.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        BaseResponse<Void> body = BaseResponse.error("You do not have permission to access this resource 12");
        objectMapper.writeValue(response.getOutputStream(), body);

    }
}
