package com.zengcode.customer.controller;

import com.zengcode.config.starter.annotation.ConfigValueHolder;
import com.zengcode.config.starter.annotation.ZengcodeConfig;
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

    @ZengcodeConfig(key = "application.name")
    private ConfigValueHolder<String> applicationName;

    @ZengcodeConfig(key = "test2")
    private ConfigValueHolder<String> test2;

    @GetMapping
    public String applicationName() {
        String valueFromHolder = applicationName != null ? applicationName.get() : "Holder is null";
        String message = "Welcome to " + configStoreService.getProperty("application.name");
        message += "\n From Annotation = " + valueFromHolder;
        message += "\n test2 = " + test2.get();
        return message;
    }
}
