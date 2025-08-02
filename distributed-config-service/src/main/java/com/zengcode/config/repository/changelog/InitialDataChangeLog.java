package com.zengcode.config.repository.changelog;

import com.zengcode.config.configuration.ConfigConstants;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import zengcode.config.common.utillity.ZengcodeConfigEnum;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ChangeUnit(id = ConfigConstants.CHANGELOG_ID, order = "001", author = "zengcode")
@Slf4j
public class InitialDataChangeLog {

    private static final String SERVICE = "customer-service";
    private static final String ENV = "dev";
    private static final String VERSION = "1.0.0";

    @Execution
    public void changeSet(MongoTemplate mongoTemplate) {
        log.info("============ Initial Data Change Log ============== {}", getCollectionName());
        Map<String, Object> configs = getConfigsMapping();
        Document initialConfig = new Document()
                .append(ZengcodeConfigEnum.SERVICE.getValue(), SERVICE)
                .append(ZengcodeConfigEnum.ENV.getValue(), ENV)
                .append(ZengcodeConfigEnum.VERSION.getValue(), VERSION)
                .append(ZengcodeConfigEnum.CONFIGS.getValue(), configs)
                .append(ZengcodeConfigEnum.LAST_UPDATED.getValue(), Instant.now());

        mongoTemplate.getCollection(getCollectionName()).insertOne(initialConfig);
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        // Rollback by deleting the single full document
        Document filter = new Document()
                .append(ZengcodeConfigEnum.SERVICE.getValue(), SERVICE)
                .append(ZengcodeConfigEnum.ENV.getValue(), ENV)
                .append(ZengcodeConfigEnum.VERSION.getValue(), VERSION);
        mongoTemplate.getCollection(getCollectionName()).deleteMany(filter);
    }

    private static Map<String, Object> getConfigsMapping() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("application.name", "Customer Service");
        configs.put("feature.signup.enabled", true);
        configs.put("feature.signup.success.notify", false);
        return configs;
    }

    private String getCollectionName() {
        return  String.format("%s_%s_%s_%s",
                ZengcodeConfigEnum.CONFIGS.getValue(),
                SERVICE,
                ENV,
                VERSION
        );
    }
}