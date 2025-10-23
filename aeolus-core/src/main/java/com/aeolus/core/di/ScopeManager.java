package com.aeolus.core.di;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ScopeManager {

    private final Map<String, Map<Class<?>, Object>> scopes = new ConcurrentHashMap<>();
    private final ThreadLocal<Map<Class<?>, Object>> threadScoped =
            ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Set<Class<?>>> threadCreating =
            ThreadLocal.withInitial(HashSet::new);

    public ScopeManager() {
        scopes.put("singleton", new ConcurrentHashMap<>());
        scopes.put("prototype", Collections.emptyMap());
    }

    private final Set<Class<?>> creating = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(String scope, Class<T> type, InstanceFactory<T> factory) {
        if ("prototype".equals(scope)) return factory.create();
        if ("thread".equals(scope)) return getOrCreatePerThread(type, factory);

        Map<Class<?>, Object> map = scopes.get(scope);
        if (map == null) throw new IllegalStateException("Unknown scope: " + scope);

        Object existing = map.get(type);
        if (existing != null) return (T) existing;

        if (!creating.add(type))
            throw new IllegalStateException("Recursive creation detected for " + type);

        try {
            T instance = factory.create();
            map.put(type, instance);
            return instance;
        } finally {
            creating.remove(type);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrCreatePerThread(Class<T> type, InstanceFactory<T> factory) {
        Map<Class<?>, Object> map = threadScoped.get();
        Object existing = map.get(type);
        if (existing != null) return (T) existing;

        Set<Class<?>> localCreating = threadCreating.get();
        if (!localCreating.add(type))
            throw new IllegalStateException("Recursive creation detected for " + type);

        try {
            T instance = factory.create();
            map.put(type, instance);
            return instance;
        } finally {
            localCreating.remove(type);
        }
    }

    public interface InstanceFactory<T> { T create(); }
}
