package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Config;

import java.lang.reflect.Field;
import java.util.Properties;

public final class PropertyBinder {

    public static Object bindConfig(Class<?> type, Properties props) {
        try {
            Config cfg = type.getAnnotation(Config.class);
            String prefix = cfg.prefix() + ".";
            Object instance = type.getDeclaredConstructor().newInstance();
            for (Field f : type.getDeclaredFields()) {
                String key = prefix + f.getName();
                if (props.containsKey(key)) {
                    f.setAccessible(true);
                    f.set(instance, convertValue(f.getType(), props.getProperty(key)));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to bind config for " + type.getName(), e);
        }
    }

    private static Object convertValue(Class<?> t, String v) {
        if (t == String.class) return v;
        if (t == int.class || t == Integer.class) return Integer.parseInt(v);
        if (t == long.class || t == Long.class) return Long.parseLong(v);
        if (t == boolean.class || t == Boolean.class) return Boolean.parseBoolean(v);
        if (t == double.class || t == Double.class) return Double.parseDouble(v);
        return v;
    }
}
