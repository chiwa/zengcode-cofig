package com.zengcode.config.starter.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zengcode.config.common.dto.ConfigPublishMessage;
import zengcode.config.common.utillity.PublishMessageOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Getter
@Setter
public class ConfigStoreService {

    private final ConcurrentHashMap<String, Object> configMap = new ConcurrentHashMap<>();
    private final AtomicReference<String> lastSnapshotId = new AtomicReference<>();

    public void apply(ConfigPublishMessage message) {
        String key = message.key();
        Object value = message.value();
        PublishMessageOperation op = message.operation();

        switch (op) {
            case INITIAL -> {
                String incomingSnapshot = message.lastSnapshotId();
                if (!incomingSnapshot.equals(lastSnapshotId.get())) {
                    configMap.clear();
                    //Store it into DB in the future if it's needed
                    lastSnapshotId.set(incomingSnapshot);
                    log.info("🔄 New Snapshot ID: {}", incomingSnapshot);
                }
                configMap.put(key, value);
            }
            case NEW, UPDATED -> configMap.put(key, value);
            case DELETED -> configMap.remove(key);
        }
        log.info("✅ Updated Config Map: {}", configMap);
    }

    public Map<String, Object> getAllConfigs() {
        return Map.copyOf(configMap);
    }

    public Object getProperty(String key) {
        return configMap.getOrDefault(key, "");
    }
    public String getLastSnapshotId() {
        return lastSnapshotId.get();
    }
}