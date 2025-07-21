package com.zengcode.config.service.kafka;

import java.util.List;
import java.util.Map;

public interface IPublishConfiguration {
    void publishAllConfiguration(List<String> collections, Map<String, Object> configMapping);
    void publishDiffConfiguration(String collection,
                                          Map<String, Object> response,
                                          Map<String, Object> configs);
}
