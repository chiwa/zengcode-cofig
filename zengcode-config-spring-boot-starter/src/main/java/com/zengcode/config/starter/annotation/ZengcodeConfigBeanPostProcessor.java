package com.zengcode.config.starter.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

@Component
public class ZengcodeConfigBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Object> configMap;

    public ZengcodeConfigBeanPostProcessor(Map<String, Object> configMap) {
        this.configMap = configMap;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            ZengcodeConfig annotation = AnnotationUtils.getAnnotation(field, ZengcodeConfig.class);
            if (annotation != null && ZengcodeConfigGetter.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                String key = annotation.key();
                ZengcodeConfigGetter<?> proxy = ZengcodeConfigProxy.create(key, configMap, ZengcodeConfigGetter.class);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject config proxy", e);
                }
            }
        }
        return bean;
    }
}