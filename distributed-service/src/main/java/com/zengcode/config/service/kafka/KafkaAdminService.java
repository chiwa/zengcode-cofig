package com.zengcode.config.service.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.segment-bytes}")
    private String segmentBytes;

    private AdminClient adminClient;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.adminClient = AdminClient.create(props);
    }

    /**
     * สร้าง Topic แบบ Log Compacted
     */
    public void createCompactTopic(String topicName) {
        int partitions = 1;
        short replicationFactor = 1;

        NewTopic topic = new NewTopic(topicName, partitions, replicationFactor)
                .configs(Map.of(
                        TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT,
                        TopicConfig.RETENTION_MS_CONFIG, "-1",
                        TopicConfig.SEGMENT_BYTES_CONFIG, segmentBytes
                ));

        try {
            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(topic));
            result.all().get(); // wait for completion
            log.info("✅ Created compact topic: {}", topicName);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.info("⚠️ Topic already exists: {}", topicName);
            } else {
                log.error("❌ Failed to create topic: {}", topicName, e);
            }
        } catch (Exception e) {
            log.error("❌ Unexpected error while creating topic: {}", topicName, e);
        }
    }

    @PreDestroy
    public void close() {
        if (adminClient != null) {
            adminClient.close();
            log.info("🧹 KafkaAdminClient closed.");
        }
    }
}