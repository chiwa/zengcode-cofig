package com.zengcode.config.starter.service;

import com.zengcode.config.starter.annotation.ZengcodeConfigRefresher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import zengcode.config.common.dto.ConfigPublishMessage;
import zengcode.config.common.utillity.PublishMessageOperation;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Getter
@Setter
public class ConfigStoreService {

    @Lazy
    private ZengcodeConfigRefresher configRefresher;
    private final ConcurrentHashMap<String, Object> configMap = new ConcurrentHashMap<>();
    private final AtomicReference<String> lastSnapshotId = new AtomicReference<>();

    @Autowired
    public void setConfigRefresher(@Lazy ZengcodeConfigRefresher configRefresher) {
        this.configRefresher = configRefresher;
    }

    public void apply(ConfigPublishMessage message) {
        String key = message.key();
        Object value = message.value();
        PublishMessageOperation op = message.operation();

        if (op == null) return;
        switch (op) {
            case INITIAL -> {
                String incomingSnapshot = message.lastSnapshotId();
                if (!Objects.equals(incomingSnapshot, lastSnapshotId.get())) {
                    configMap.clear();
                    lastSnapshotId.set(incomingSnapshot);
                    log.info("🔄 New Snapshot ID: {}", incomingSnapshot);
                }

                configMap.put(key, value);
                configRefresher.refreshConfigTyped(key, value); // ✅ refresh ทุก holder
            }

            case NEW, UPDATED -> {
                configMap.put(key, value);
                configRefresher.refreshConfigTyped(key, value); // ✅ refresh ทุก holder
            }

            case DELETED -> {
                configMap.remove(key);
                configRefresher.removeConfig(key); // ✅ clear จากทุก holder
            }
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