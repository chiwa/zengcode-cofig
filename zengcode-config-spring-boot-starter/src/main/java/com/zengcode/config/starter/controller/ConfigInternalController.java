package com.zengcode.config.starter.controller;

import com.zengcode.config.starter.service.ConfigStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/configs")
@RequiredArgsConstructor
public class ConfigInternalController {

    private final ConfigStoreService configStoreService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        return ResponseEntity.ok(configStoreService.getAllConfigs());
    }
}
