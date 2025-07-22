package com.zengcode.config.starter.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zengcode.config.starter.config.ZengcodeConfigProperties;
import com.zengcode.config.starter.service.ConfigStoreService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;
import zengcode.config.common.dto.ConfigPublishMessage;

import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConfigKafkaConsumerRoute extends RouteBuilder {

    private final ZengcodeConfigProperties configProps;
    private final ConfigStoreService configStoreService;

    @Override
    public void configure() {

        String topic = String.format("configs_%s_%s_%s",
                configProps.getService(),
                configProps.getEnv(),
                configProps.getVersion());
        String brokers = configProps.getKafka().getBootStrap();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(ConfigPublishMessage.class);
        jacksonDataFormat.setObjectMapper(mapper);

        String groupId = configProps.getService() + UUID.randomUUID();

        log.info("=================================================================");
        log.info("topic : {}", topic);
        log.info("groupId : {}", groupId);
        log.info("==================================================================");

        from(String.format("kafka:%s?brokers=%s&groupId=%s&autoOffsetReset=earliest", topic,  brokers, groupId))
                .routeId("config-topic-consumer")
                .log("📥 Raw: ${body}")
                .choice()
                .when(body().isNull())
                .log(LoggingLevel.DEBUG, "❌ Null body! Skip message")
                .otherwise()
                .unmarshal(jacksonDataFormat)
                .log(LoggingLevel.INFO,"✅ Deserialized: ${body}")
                .process(exchange -> {
                    ConfigPublishMessage message = exchange.getIn().getBody(ConfigPublishMessage.class);
                    configStoreService.apply(message);
                });
    }
}