package com.zengcode.config.service;

import com.zengcode.config.repository.mongodb.IConfigMappingRepository;
import com.zengcode.config.service.kafka.IPublishConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import zengcode.config.common.dto.ConfigPublishMessage;
import zengcode.config.common.dto.ConfigRequest;
import zengcode.config.common.exception.ResolveConfigurationException;
import zengcode.config.common.utillity.PublishMessageOperation;
import zengcode.config.common.utillity.ZengcodeConfigEnum;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationService implements IConfiguration {

    private final MongoTemplate mongoTemplate;
    private final IConfigMappingRepository configDocumentRepository;
    private final KafkaTemplate kafkaTemplate;
    private final IPublishConfiguration publishConfiguration;

    @Override
    public Map<String, Object> resolveConfiguration(String service,
                                                    String environment,
                                                    String version) {
        log.info("Resolve Configuration Star, service : {}, env : {}, version : {}"
        ,service , environment, version);
        String normalizedService = normalize(service);
        String normalizedEnv = normalize(environment);
        String normalizedVersion = normalize(version);
        try {
            return configDocumentRepository
                    .findByServiceEnvVersion(normalizedService,
                                             normalizedEnv,
                                             normalizedVersion);

        } catch (Exception e) {
            log.error("Resolve Configuration Exception : {} ", e.getMessage());
            throw new ResolveConfigurationException(e.getMessage());
        }
    }

    @Override
    public void upsertConfigurations(ConfigRequest configRequest) {
        Map<String, Object> response = configDocumentRepository.upsert(configRequest);
        publishConfiguration
                .publishDiffConfiguration(
                        (String) response.get(ZengcodeConfigEnum.COLLECTION_NAME.getValue()),
                        response,
                        configRequest.getConfigs()
                );
    }

    @Override
    public List<String> getAllConfigurations() {
        return configDocumentRepository.getAllConfigurations()
                .stream()
                .filter(name -> name.startsWith(ZengcodeConfigEnum.CONFIGS.getValue()))
                .toList();
    }

    @Override
    public Map<String, Object> resolveConfigurationFromCollectionName(String collectionName) {
        log.info("📂 Resolving config directly from collection: {}", collectionName);
        return configDocumentRepository.findByCollectionName(collectionName);
    }

    private String normalize(String input) {
        return StringUtils.isEmpty(input) ? "" : input.trim().toLowerCase();
    }
}
