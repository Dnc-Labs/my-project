package com.ecommerce.api.controllers;

import com.ecommerce.api.dto.AppInfoResponse;
import com.ecommerce.api.services.AppInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppInfoController {

    private final AppInfoService appInfoService;

    public AppInfoController(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    @GetMapping("/info")
    public ResponseEntity<AppInfoResponse> getAppInfo() {
        AppInfoResponse appInfoResponse = this.appInfoService.getAppInfo();
        return ResponseEntity.ok(appInfoResponse);
    }
}
