package com.ecommerce.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Spring MVC — đặc biệt là resource handler để serve static files
 * trong folder uploads/ qua URL /uploads/**.
 *
 * Chỉ active khi storage.type=local (S3/Cloudinary tự serve qua CDN).
 *
 * Resource handler chạy KHÔNG qua Spring controller chain → fast.
 * Spring đọc file từ disk, stream về client.
 */
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String publicUrlPrefix;

    public WebMvcConfig(
            @Value("${storage.local.upload-dir}") String uploadDir,
            @Value("${storage.local.public-url-prefix}") String publicUrlPrefix) {
        this.uploadDir = uploadDir;
        this.publicUrlPrefix = publicUrlPrefix;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL "/uploads/**" → đọc từ folder local (./uploads/)
        // file: prefix = filesystem path (ngược với classpath:)
        // Trailing "/" bắt buộc — Spring resolve relative paths
        registry.addResourceHandler(publicUrlPrefix + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
