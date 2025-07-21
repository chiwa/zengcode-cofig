package com.zengcode.config.repository.mongodb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import zengcode.config.common.dto.ConfigRequest;
import zengcode.config.common.utillity.PublishMessageOperation;
import zengcode.config.common.utillity.ZengcodeConfigEnum;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MongoDBConfigMappingRepository implements IConfigMappingRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Map<String, Object> findByServiceEnvVersion(String service, String env, String version) {

        String collectionName = String.format("%s_%s_%s_%s",
                ZengcodeConfigEnum.CONFIGS.getValue(),
                service,
                env,
                version
        );
        log.info("📦 Reading config from collection: {}", collectionName);

        // Query เพื่อดึง document เดียวที่ตรงกับ service/env/version
        Document query = new Document()
                .append(ZengcodeConfigEnum.SERVICE.getValue(), service)
                .append(ZengcodeConfigEnum.ENV.getValue(), env)
                .append(ZengcodeConfigEnum.VERSION.getValue(), version);

        Document document = mongoTemplate.getCollection(collectionName).find(query).first();

        if (document == null) {
            log.warn("No config found for service={}, env={}, version={}", service, env, version);
            return new LinkedHashMap<>();
        }

        log.info("📄 Raw Document: {}", document.toJson());

        @SuppressWarnings("unchecked")
        Map<String, Object> configs = document.get("configs", Map.class);

        if (configs == null) {
            log.warn("Configs field is missing or empty");
            return new LinkedHashMap<>();
        }

        // log ค่าแต่ละตัว
        configs.forEach((k, v) -> log.info("Parsed: key={}, value={}", k, v));

        return new LinkedHashMap<>(configs);
    }

    @Override
    public Map<String, Object> upsert(ConfigRequest configRequest) {
        String collectionName = String.format("%s_%s_%s_%s",
                ZengcodeConfigEnum.CONFIGS.getValue(),
                configRequest.getService(),
                configRequest.getEnv(),
                configRequest.getVersion()
        );

        Map<String, Object> newConfigs = configRequest.getConfigs();
        if (newConfigs == null || newConfigs.isEmpty()) {
            throw new IllegalArgumentException("ConfigMapping must not be empty");
        }

        // 1. อ่าน config เก่า
        Query query = new Query(Criteria.where(ZengcodeConfigEnum.SERVICE.getValue()).is(configRequest.getService())
                .and(ZengcodeConfigEnum.ENV.getValue()).is(configRequest.getEnv())
                .and(ZengcodeConfigEnum.VERSION.getValue()).is(configRequest.getVersion()));

        Map<String, Object> oldConfigs = Optional.ofNullable(
                        mongoTemplate.findOne(query, Map.class, collectionName)
                ).map(doc -> (Map<String, Object>) doc.get(ZengcodeConfigEnum.CONFIGS.getValue()))
                .orElse(Collections.emptyMap());

        // 2. สร้างชุด diff
        Set<String> addedKeys = new HashSet<>(newConfigs.keySet());
        addedKeys.removeAll(oldConfigs.keySet());

        Set<String> deletedKeys = new HashSet<>(oldConfigs.keySet());
        deletedKeys.removeAll(newConfigs.keySet());

        Set<String> updatedKeys = new HashSet<>();
        for (String key : newConfigs.keySet()) {
            if (oldConfigs.containsKey(key)) {
                Object oldVal = oldConfigs.get(key);
                Object newVal = newConfigs.get(key);
                if (!Objects.equals(oldVal, newVal)) {
                    updatedKeys.add(key);
                }
            }
        }
        log.info("➕ Added: {}", addedKeys);
        log.info("📝 Updated: {}", updatedKeys);
        log.info("❌ Deleted: {}", deletedKeys);



        // 4. upsert ใหม่
        Update update = new Update();
        update.set(ZengcodeConfigEnum.SERVICE.getValue(), configRequest.getService());
        update.set(ZengcodeConfigEnum.ENV.getValue(), configRequest.getEnv());
        update.set(ZengcodeConfigEnum.VERSION.getValue(), configRequest.getVersion());
        update.set(ZengcodeConfigEnum.CONFIGS.getValue(), configRequest.getConfigs());
        update.set(ZengcodeConfigEnum.LAST_UPDATED.getValue(), new Date());
        mongoTemplate.upsert(query, update, collectionName);

        // 5. สร้าง response
        Map<String, Object> response = new HashMap<>(newConfigs);
        response.put(ZengcodeConfigEnum.SERVICE.getValue(), configRequest.getService());
        response.put(ZengcodeConfigEnum.ENV.getValue(), configRequest.getEnv());
        response.put(ZengcodeConfigEnum.VERSION.getValue(), configRequest.getVersion());
        response.put(ZengcodeConfigEnum.COLLECTION_NAME.getValue(), collectionName);
        response.put(ZengcodeConfigEnum.LAST_UPDATED.getValue(), new Date());
        response.put(ZengcodeConfigEnum.CONFIGS.getValue(), configRequest.getConfigs());
        response.put(PublishMessageOperation.NEW.getValue(), addedKeys);
        response.put(PublishMessageOperation.DELETED.getValue(), deletedKeys);
        response.put(PublishMessageOperation.UPDATED.getValue(), updatedKeys);
        return response;
    }
    @Override
    public Set<String> getAllConfigurations() {
        String dbName = mongoTemplate.getDb().getName();
        log.info("📛 MongoDB Database: {}", dbName);

        Set<String> allCollections = mongoTemplate.getCollectionNames();
        log.info("📂 All collections: {}", allCollections);

        Set<String> configCollections = allCollections.stream()
                .filter(name -> name.startsWith("configs_"))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        log.info("✅ Filtered config collections: {}", configCollections);
        return configCollections;
    }

    @Override
    public Map<String, Object> findByCollectionName(String collectionName) {
        // ดึง document แรกใน collection นั้น
        Document document = mongoTemplate
                .getCollection(collectionName)
                .find()
                .first();

        if (document == null) {
            log.warn("⚠️ No document found in collection: {}", collectionName);
            return Collections.emptyMap();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> configs = document.get("configs", Map.class);

        if (configs == null) {
            log.warn("⚠️ Document found but 'configs' field is null in collection: {}", collectionName);
            return Collections.emptyMap();
        }

        log.info("✅ Loaded {} config entries from {}", configs.size(), collectionName);
        return new LinkedHashMap<>(configs);
    }
}
