package com.zengcode.config.starter.annotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

public class ZengcodeConfigProxy {

    @SuppressWarnings("unchecked")
    public static <T> T create(String key, Map<String, Object> configMap, Class<T> clazz) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("get".equals(method.getName())) {
                Object result = configMap.get(key);
                if (result == null) {
                    throw new IllegalStateException("❌ There is no config for key = " + key);
                }
                return result;
            }
            throw new UnsupportedOperationException("Only get() is supported");
        };

        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                handler
        );
    }
}