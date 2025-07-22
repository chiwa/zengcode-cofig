package com.zengcode.config.starter.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ZengcodeConfigRefresher implements ApplicationListener<ApplicationReadyEvent> {

    @Qualifier("configMap")
    private ConcurrentHashMap<String, Object> configMap;
    private final ApplicationContext context;

    public ZengcodeConfigRefresher(@Qualifier("configMap") ConcurrentHashMap<String, Object> configMap,
                                   ApplicationContext context) {
        this.configMap = configMap;
        this.context = context;
    }

    private final Map<String, List<ConfigValueHolder<?>>> configKeyToHolders = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (Object bean : context.getBeansWithAnnotation(Component.class).values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                ZengcodeConfig annotation = field.getAnnotation(ZengcodeConfig.class);

                if (annotation != null && ConfigValueHolder.class.isAssignableFrom(field.getType())) {
                    String key = annotation.key();
                    field.setAccessible(true);

                    try {
                        //Always override with a Lazy Holder
                        ConfigValueHolder<?> lazyHolder = new ConfigValueHolder<>(() -> configMap.getOrDefault(key, ""));
                        field.set(bean, lazyHolder);

                        //Register for runtime refresh
                        configKeyToHolders
                                .computeIfAbsent(key, k -> new ArrayList<>())
                                .add(lazyHolder);

                        log.info("Injected Lazy Config Holder for key = {}, value = {}", key, configMap.getOrDefault(key, ""));

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to inject config value", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void refreshConfigTyped(String key, Object newValue) {
        List<ConfigValueHolder<?>> holders = configKeyToHolders.get(key);
        if (holders != null) {
            for (ConfigValueHolder<?> holder : holders) {
                // 💡 cast ตัว holder และ newValue ให้กลายเป็นชนิดเดียวกัน
                ConfigValueHolder<Object> objectHolder = (ConfigValueHolder<Object>) holder;
                objectHolder.set(newValue);
                objectHolder.overrideLazy(() -> newValue);
            }
        }
    }

    public void removeConfig(String key) {
        List<ConfigValueHolder<?>> holders = configKeyToHolders.get(key);
        if (holders != null) {
            for (ConfigValueHolder<?> holder : holders) {
                holder.set(null); // เคลียร์ค่า
                holder.overrideLazy(() -> null); // เคลียร์ lazy loader ด้วย
            }
        }
    }
}