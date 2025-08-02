package com.zengcode.config.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/health")
public class HealthCheck {

    @GetMapping
    public String ping() {
        return "SERVICE IS AVAILABLE";
    }
}
