package com.zengcode.config.repository.mongodb;

import zengcode.config.common.dto.ConfigRequest;

import java.util.Map;
import java.util.Set;

public interface IConfigMappingRepository {
     Map<String, Object> findByServiceEnvVersion(String service, String env, String version) ;
     Map<String, Object> upsert(ConfigRequest configRequest);
     Set<String> getAllConfigurations();
     Map<String, Object> findByCollectionName(String collectionName) ;
}
