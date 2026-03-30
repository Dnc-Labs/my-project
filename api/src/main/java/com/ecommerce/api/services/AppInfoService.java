package com.ecommerce.api.services;

import com.ecommerce.api.dto.AppInfoResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppInfoService {

    private static final Logger log = LoggerFactory.getLogger(AppInfoService.class);

    private final String appName;
    private final String appVersion;
    private final String appDescription;

    public AppInfoService(
            @Value("${app.name}") String appName,
            @Value("${app.version}") String appVersion,
            @Value("${app.description}") String appDescription) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.appDescription = appDescription;
    }

    @PostConstruct
    private void init() {
        log.info("AppInfoService initialized — {} v{}", appName, appVersion);
    }

    @PreDestroy
    private void destroy() {
        log.info("AppInfoService destroyed");
    }

    public AppInfoResponse getAppInfo() {
        return new AppInfoResponse(appName, appVersion, appDescription);
    }
}
