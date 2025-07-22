package com.zengcode.config.starter.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableConfigurationProperties({ZengcodeConfigProperties.class})
@ComponentScan("com.zengcode.config.starter")
@Slf4j
public class ZengcodeConfigAutoConfiguration {

    @PostConstruct
    public void logLoad() {
        log.info("======================================================");
        log.info("✅✅✅ LOADED ZengcodeConfigAutoConfiguration ✅✅✅");
        log.info("======================================================");
    }

    @Bean
    public ConcurrentHashMap<String, Object> configMap() {
        return new ConcurrentHashMap<>();
    }
}