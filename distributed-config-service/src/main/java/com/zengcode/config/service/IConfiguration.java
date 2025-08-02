package com.zengcode.config.service;

import zengcode.config.common.dto.ConfigRequest;

import java.util.List;
import java.util.Map;

public interface IConfiguration {
    Map<String, Object> resolveConfiguration(String service,
                                                String environment,
                                                String version);

    void upsertConfigurations(ConfigRequest configRequest);
    List<String> getAllConfigurations();
    Map<String, Object> resolveConfigurationFromCollectionName(String collectionName);
}
