package com.zengcode.config.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import zengcode.config.common.dto.ConfigPublishMessage;
import zengcode.config.common.utillity.PublishMessageOperation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublishConfigurationKafka implements IPublishConfiguration {

    private static final String CONFIG_PUBLISHER_LOCK = "config-publisher-lock";
    private final KafkaAdminService kafkaAdminService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedissonClient redissonClient;

    @Override
    public void publishAllConfiguration(List<String> collections, Map<String, Object> configMapping) {

                String lastSnapshotId = UUID.randomUUID().toString();
                for (String collection : collections) {
                    log.info("🔐 Publish Configuration to Kafka topic: {}", collection);
                    kafkaAdminService.createCompactTopic(collection);

                   configMapping
                            .forEach((key, value) -> {
                                ConfigPublishMessage message = new ConfigPublishMessage(
                                        collection,
                                        PublishMessageOperation.INITIAL,
                                        key,
                                        value,
                                        lastSnapshotId
                                );
                                log.info("🚀 Sending key={}, message={}, to topic={}", key, message, collection);
                                kafkaTemplate.send(collection, key, message);
                            });
                }
    }

    @Override
    public void publishDiffConfiguration(String collection,
                                         Map<String, Object> response,
                                         Map<String, Object> configs) {
        for (PublishMessageOperation op : PublishMessageOperation.values()) {
            String opKey = op.getValue();
            Object raw = response.get(opKey);

            if (raw instanceof Set<?> keySet) {
                keySet.forEach(keyObj -> {
                    String key = keyObj.toString();
                    Object actualValue = (op == PublishMessageOperation.DELETED) ? null : configs.get(key);

                    // ✅ 1. Always send ConfigPublishMessage (even DELETED)
                    ConfigPublishMessage message = new ConfigPublishMessage(
                            collection,
                            op,
                            key,
                            actualValue,
                            ""
                    );
                    log.info("🚀 Sending key={}, op={}, message={} to topic={}", key, op, message, collection);
                    kafkaTemplate.send(collection, key, message);

                    // ✅ 2. For DELETED → also send tombstone
                    if (op == PublishMessageOperation.DELETED) {
                        log.info("🧹 Sending tombstone for key={}, topic={}", key, collection);
                        kafkaTemplate.send(collection, key, null);
                    }
                });
            } else if (raw != null) {
                log.warn("⚠️ Unexpected type for opKey='{}': {}", opKey, raw.getClass().getName());
            }
        }
    }
}
