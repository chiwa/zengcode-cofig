package com.zengcode.config.starter.annotation;

import java.util.function.Supplier;

public class ConfigValueHolder<T> {
    private volatile T value;
    private Supplier<T> lazyLoader;

    public ConfigValueHolder(Supplier<T> loader) {
        this.lazyLoader = loader;
    }

    public T get() {
        if (value == null && lazyLoader != null) {
            value = lazyLoader.get();
        }
        if (value == null) {
            throw new IllegalStateException("No value available for config. Both direct value and lazy loader returned null.");
        }
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void overrideLazy(Supplier<T> newLoader) {
        this.lazyLoader = newLoader;
    }
}