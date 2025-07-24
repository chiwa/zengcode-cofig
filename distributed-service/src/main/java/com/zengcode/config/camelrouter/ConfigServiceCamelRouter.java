package com.zengcode.config.camelrouter;

import com.zengcode.config.configuration.ConfigConstants;
import com.zengcode.config.service.IConfiguration;
import com.zengcode.config.service.kafka.IPublishConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import zengcode.config.common.utillity.DistributedLock;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigServiceCamelRouter extends RouteBuilder {

    private static final String CONFIG_PUBLISHER_LOCK = "config-publisher-lock";
    private final IConfiguration configurationService;
    private final IPublishConfiguration publishKafka;
    private final MongoTemplate mongoTemplate;
    private final DistributedLock distributedLock;


    @Override
    public void configure() {
        //Create Routing in the future id needed.
    }

    @EventListener(ApplicationReadyEvent.class)
    public void waitForChangelogThenPublish() {
        distributedLock.withDistributedLock(CONFIG_PUBLISHER_LOCK, 5, 120, () -> {
            await().atMost(15, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .until(() -> isChangelogApplied(ConfigConstants.CHANGELOG_ID));

            log.info("✅ Changelog '{}' applied. Starting configuration publish...", ConfigConstants.CHANGELOG_ID);

            configurationService.getAllConfigurations()
                    .forEach(collection -> {
                        publishKafka.publishAllConfiguration(
                                configurationService.getAllConfigurations(),
                                configurationService.resolveConfigurationFromCollectionName(collection)
                        );
                    });

            return null;
        });
    }

    private boolean isChangelogApplied(String changeId) {
        return mongoTemplate.exists(
                Query.query(Criteria.where("changeId").is(changeId).and("state").is("EXECUTED")),
                "mongockChangeLog"
        );
    }
}