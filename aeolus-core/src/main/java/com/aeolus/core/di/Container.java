package com.aeolus.core.di;

import com.aeolus.core.di.annotations.*;
import com.aeolus.core.di.annotations.Scope;
import com.aeolus.core.di.exceptions.*;
import com.aeolus.core.logging.*;
import jakarta.inject.*;
import jakarta.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aeolus DI Container
 * ------------------------------------------------------------
 * Features:
 *  - @Inject constructor, field, setter
 *  - @Scope("singleton"/"prototype"/"thread")
 *  - @Lazy, @Config(prefix)
 *  - @PostConstruct, @PreDestroy lifecycle hooks
 *  - @Resource(name="key") property injection
 *  - BeanProcessor hooks
 *  - container.create() manual wiring
 *  - container.stats() runtime introspection
 *  - Metadata cache via .aeolus.cache
 */
public final class Container implements AutoCloseable {

    private final Map<Class<?>, Class<?>> bindings = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> namedBindings = new ConcurrentHashMap<>();
    private final Map<String, Object> namedBeans = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    private final Set<Object> managedInstances = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final List<BeanProcessor> beanProcessors = new ArrayList<>();
    private final ScopeManager scopeManager = new ScopeManager();
    private final Properties properties = new Properties();

    private Logger log;

    private Container(Logger logger) {
        this.log = (logger != null) ? logger : new ConsoleLogger();
    }

    // ------------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------------
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Container container = new Container(new ConsoleLogger());

        public Builder logger(Logger logger) {
            container.log = (logger != null) ? logger : new ConsoleLogger();
            container.log.info("Using custom logger: %s", container.log.getClass().getSimpleName());
            return this;
        }

        public Builder scan(String... pkgs) {
            Set<Class<?>> classes = ComponentScanner.scan(pkgs);
            if (!classes.isEmpty()) {
                MetadataCache.save(classes);
                container.log.info("Scanned packages %s → %d components", Arrays.toString(pkgs), classes.size());
            } else {
                classes = MetadataCache.load(pkgs);
                if (!classes.isEmpty())
                    container.log.info("Loaded %d components from cache", classes.size());
                else
                    container.log.warn("No components discovered for %s", Arrays.toString(pkgs));
            }
            for (Class<?> c : classes) {
                //TODO: Move this to Strategy pattern implementation for future extensibility
                if (c.isAnnotationPresent(Configuration.class))
                    container.registerConfiguration(c);
                else if (c.isAnnotationPresent(Component.class) || c.isAnnotationPresent(Singleton.class))
                    container.registerComponent(c);
            }
            return this;
        }

        public Builder loadProperties(String file) {
            try (FileInputStream in = new FileInputStream(file)) {
                container.properties.load(in);
                container.log.info("Loaded properties: %s (%d entries)", file, container.properties.size());
            } catch (IOException e) {
                container.log.warn("No properties file found: %s", file);
            }
            return this;
        }

        public Builder addProcessor(BeanProcessor processor) {
            container.beanProcessors.add(processor);
            container.log.info("Registered BeanProcessor: %s", processor.getClass().getSimpleName());
            return this;
        }

        public Container build() {
            container.log.info("Container initialized with %d bindings, %d named beans",
                    container.bindings.size(), container.namedBeans.size());
            return container;
        }
    }

    // ------------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------------
    public <T> T get(Class<T> type) {
        return resolve(type, null, new HashSet<>());
    }

    public Object getByName(String name) {
        Object bean = namedBeans.get(name);
        if (bean != null) return bean;

        Class<?> type = namedBindings.get(name);
        if (type != null)
            return resolve(type, name, new HashSet<>());

        throw new ResourceMissingException("No bean named: " + name);
    }

    public <T> T create(Class<T> type) {
        return instantiate(type, new HashSet<>());
    }

    public Map<String, Object> stats() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("bindings", bindings.size());
        map.put("beans", beans.size());
        map.put("named", namedBeans.size());
        map.put("managed", managedInstances.size());
        map.put("properties", properties.size());
        map.put("processors", beanProcessors.size());
        map.put("memory.used.mb",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
        return map;
    }

    @Override
    public void close() {
        log.info("Container shutting down (%d managed beans)...", managedInstances.size());
        managedInstances.forEach(this::invokePreDestroy);
    }

    // ------------------------------------------------------------------------
    // Resolution Core
    // ------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private <T> T resolve(Class<T> type, String name, Set<Class<?>> stack) {
        if (stack.contains(type))
            throw new CircularDependencyException("Circular dependency detected: " + stack + " → " + type);
        stack.add(type);

        try {
            // @Config binding
            if (type.isAnnotationPresent(Config.class))
                return (T) PropertyBinder.bindConfig(type, properties);

            if (name != null) {
                Object named = namedBeans.get(name);
                if (named != null) return (T) named;

                Class<?> namedType = namedBindings.get(name);
                if (namedType != null) {
                    T resolved = instantiate((Class<T>) namedType, stack);
                    if (shouldCacheNamedInstance(namedType))
                        namedBeans.putIfAbsent(name, resolved);
                    return resolved;
                }
            }

            if (beans.containsKey(type)) return (T) beans.get(type);

            Class<?> impl = bindings.getOrDefault(type, type);

            // ✅ Explicitly cast impl to Class<T>
            return instantiate((Class<T>) impl, stack);
        } finally {
            stack.remove(type);
        }
    }


    private <T> T instantiate(Class<T> impl, Set<Class<?>> stack) {
        try {
            Scope scope = impl.getAnnotation(Scope.class);
            String scopeName = (scope != null) ? scope.value() : "singleton";
            return scopeManager.getOrCreate(scopeName, impl, () -> doInstantiate(impl, stack));
        } catch (AeolusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException("Failed to instantiate " + impl.getName(), e);
        }
    }

    private <T> T doInstantiate(Class<T> impl, Set<Class<?>> stack) {
        try {

            Constructor<?> ctor = selectConstructor(impl);
            Object[] args = Arrays.stream(ctor.getParameters())
                    .map(p -> resolve(p.getType(), getName(p), stack))
                    .toArray();
            Object instance = ctor.newInstance(args);

            injectFields(impl, instance, stack);
            injectSetters(impl, instance, stack);
            injectResources(impl, instance);

            // BeanProcessors
            for (BeanProcessor p : beanProcessors)
                instance = p.postProcessBeforeInitialization(instance);

            invokePostConstruct(instance);

            for (BeanProcessor p : beanProcessors)
                instance = p.postProcessAfterInitialization(instance);

            managedInstances.add(instance);
            log.trace("Created bean: %s", impl.getSimpleName());
            return impl.cast(instance);
        } catch (AeolusException e) {
            throw e;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof AeolusException ae) throw ae;
            if (cause instanceof RuntimeException re) throw re;
            throw new BeanCreationException("Failed to create instance of " + impl.getName(), e);
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException("Failed to create instance of " + impl.getName(), e);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private <T> T createLazyProxy(Class<T> type, Set<Class<?>> stack) {
        Object proxy = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (p, m, args) -> m.invoke(resolve(type, null, stack), args)
        );
        log.trace("Created @Lazy proxy for %s", type.getSimpleName());
        return type.cast(proxy);
    }

    private void injectFields(Class<?> impl, Object instance, Set<Class<?>> stack) throws IllegalAccessException {
        for (Field f : impl.getDeclaredFields()) {
            if (f.isAnnotationPresent(Inject.class)) {
                f.setAccessible(true);
                Object dep = resolve(f.getType(), getName(f), stack);
                f.set(instance, dep);
                log.trace("Injected field %s.%s", impl.getSimpleName(), f.getName());
            }
        }
    }

    private void injectSetters(Class<?> impl, Object instance, Set<Class<?>> stack)
            throws InvocationTargetException, IllegalAccessException {
        for (Method m : impl.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Inject.class) && m.getParameterCount() == 1) {
                Object dep = resolve(m.getParameterTypes()[0], getName(m.getParameters()[0]), stack);
                m.setAccessible(true);
                m.invoke(instance, dep);
                log.trace("Injected setter %s.%s()", impl.getSimpleName(), m.getName());
            }
        }
    }

    private void injectResources(Class<?> impl, Object instance) throws IllegalAccessException {
        for (Field f : impl.getDeclaredFields()) {
            if (f.isAnnotationPresent(Resource.class)) {
                f.setAccessible(true);
                String key = f.getAnnotation(Resource.class).name();
                String value = properties.getProperty(key);
                if (value == null)
                    throw new ResourceMissingException("Missing @Resource key: " + key);
                f.set(instance, convertValue(f.getType(), value));
                log.trace("Injected @Resource %s=%s", key, value);
            }
        }
    }

    private void invokePostConstruct(Object instance) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PostConstruct.class)) {
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                    log.trace("PostConstruct executed: %s.%s()", instance.getClass().getSimpleName(), m.getName());
                } catch (Exception e) {
                    log.error("PostConstruct failed for %s: %s", instance.getClass().getSimpleName(), e);
                }
            }
        }
    }

    private void invokePreDestroy(Object instance) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PreDestroy.class)) {
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                    log.trace("PreDestroy executed: %s.%s()", instance.getClass().getSimpleName(), m.getName());
                } catch (Exception e) {
                    log.error("PreDestroy failed for %s: %s", instance.getClass().getSimpleName(), e);
                }
            }
        }
    }

    private Constructor<?> selectConstructor(Class<?> impl) {
        return Arrays.stream(impl.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        Constructor<?> c = impl.getDeclaredConstructor();
                        c.setAccessible(true);
                        return c;
                    } catch (Exception e) {
                        throw new BeanCreationException("No valid constructor for " + impl.getName(), e);
                    }
                });
    }

    // ------------------------------------------------------------------------
    // Registration Helpers
    // ------------------------------------------------------------------------
    private void registerComponent(Class<?> cls) {
        Named named = cls.getAnnotation(Named.class);
        if (named != null)
            namedBindings.put(named.value(), cls);

        Class<?>[] interfaces = cls.getInterfaces();
        if (interfaces.length == 1)
            bindings.put(interfaces[0], cls);
        else
            bindings.put(cls, cls);

        log.trace("Registered component: %s", cls.getSimpleName());
    }

    private void registerConfiguration(Class<?> cfg) {
        try {
            Object config = cfg.getDeclaredConstructor().newInstance();
            log.trace("Processing @Configuration: %s", cfg.getSimpleName());

            for (Method m : cfg.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Bean.class)) {
                    Object[] args = Arrays.stream(m.getParameters())
                            .map(p -> resolve(p.getType(), getName(p), new HashSet<>()))
                            .toArray();
                    Object bean = m.invoke(config, args);
                    Named named = m.getAnnotation(Named.class);
                    if (named != null) namedBeans.put(named.value(), bean);
                    else beans.put(m.getReturnType(), bean);
                    log.trace("Registered @Bean %s -> %s", m.getName(), bean.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            throw new BeanCreationException("Failed to process @Configuration " + cfg.getName(), e);
        }
    }

    // ------------------------------------------------------------------------
    // Utility Helpers
    // ------------------------------------------------------------------------
    private String getName(AnnotatedElement e) {
        Named named = e.getAnnotation(Named.class);
        return (named != null) ? named.value() : null;
    }

    private boolean shouldCacheNamedInstance(Class<?> type) {
        Scope scope = type.getAnnotation(Scope.class);
        return scope == null || "singleton".equals(scope.value());
    }

    private Object convertValue(Class<?> type, String value) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        return value;
    }
}
