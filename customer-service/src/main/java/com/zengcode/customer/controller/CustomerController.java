package com.zengcode.customer.controller;

import com.zengcode.config.starter.service.ConfigStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ConfigStoreService configStoreService;

    @GetMapping
    public String applicationName() {
        return "Welcome to " + configStoreService.getProperty("application.name");
    }
}
