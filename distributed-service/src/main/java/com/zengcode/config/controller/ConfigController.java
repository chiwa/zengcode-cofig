package com.zengcode.config.controller;

import com.zengcode.config.service.IConfiguration;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zengcode.config.common.dto.ConfigRequest;
import zengcode.config.common.dto.ConfigResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/configs")
@RequiredArgsConstructor
public class ConfigController {

    public final IConfiguration configurationService;

    /**
     * This API Provide the list of Configuration by Service, Environment and Version
     * @param service
     * @param environment
     * @param version
     * @return
     */
    @GetMapping("/{service}/{environment}/{version}")
    public ResponseEntity<ConfigResponse> resolveConfiguration(
            @PathVariable String service,
            @PathVariable String environment,
            @PathVariable String version) {

        Map<String, Object> configMap = configurationService.resolveConfiguration(service, environment, version);
        ConfigResponse response = new ConfigResponse(service, environment, version, configMap);
        return ResponseEntity.ok(response);
    }

    /**
     * This API Upsert the configuration for Collection
     * @param configRequest
     * @return
     */
    @PutMapping("")
    public ConfigRequest addConfiguration(@RequestBody @NotNull ConfigRequest configRequest) {
        configurationService.upsertConfigurations(configRequest);
        return configRequest;
    }

    @GetMapping()
    public List<String> getAllConfigurations() {
       return configurationService.getAllConfigurations();
    }
}