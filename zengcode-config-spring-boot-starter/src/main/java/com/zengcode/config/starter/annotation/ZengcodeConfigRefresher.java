package com.zengcode.config.starter.annotation;

import com.zengcode.config.starter.service.ConfigStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ZengcodeConfigRefresher implements ApplicationListener<ApplicationReadyEvent> {

    private final ConfigStoreService configStoreService;
    private final ApplicationContext context;

    private final Map<String, List<ConfigValueHolder<?>>> configKeyToHolders = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("==================================");
        log.info("======= Zengcode Config Init =======");
        log.info("==================================");

        for (Object bean : context.getBeansWithAnnotation(Component.class).values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                ZengcodeConfig annotation = field.getAnnotation(ZengcodeConfig.class);

                if (annotation != null && ConfigValueHolder.class.isAssignableFrom(field.getType())) {
                    String key = annotation.key();
                    field.setAccessible(true);

                    try {
                        // ⚠️ Always override with a Lazy Holder
                        ConfigValueHolder<?> lazyHolder = new ConfigValueHolder<>(() -> configStoreService.getProperty(key));
                        field.set(bean, lazyHolder);

                        // ✅ Register for runtime refresh
                        configKeyToHolders
                                .computeIfAbsent(key, k -> new ArrayList<>())
                                .add(lazyHolder);

                        log.info("🔧 Injected Lazy Config Holder for key = {}", key);

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