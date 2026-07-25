package com.ecommerce.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bind toàn bộ block 'vnpay' trong application.yaml vào một object typesafe.
 *
 * @ConfigurationProperties: Spring tự map field theo "relaxed binding"
 *   (yaml kebab-case 'tmn-code' ↔ Java camelCase 'tmnCode').
 * @Component: đăng ký làm bean để inject vào VnpayService như dependency thường.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment-gate.vnpay")
public class VnpayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String ipnUrl;
    private String version;
    private String command;
    private String locale;
}
