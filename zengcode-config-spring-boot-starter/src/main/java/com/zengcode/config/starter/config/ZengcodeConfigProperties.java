package com.zengcode.config.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "zengcode.config")
public class ZengcodeConfigProperties {
    private String service;
    private String version;
    private String env;
    private Kafka kafka;

    @Data
    public static class Kafka {
        private String bootStrap;
    }
}
