package com.ecommerce.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageable = new PageableHandlerMethodArgumentResolver();
        pageable.setMaxPageSize(100);
        pageable.setFallbackPageable(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));
        resolvers.add(pageable);
    }
}
