package com.aeolus.core.di;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class MetadataCache {

    private static final String CACHE_FILE = ".aeolus.cache";

    public static void save(Set<Class<?>> components) {
        if (components == null || components.isEmpty()) return;
        try (FileWriter fw = new FileWriter(CACHE_FILE)) {
            for (Class<?> cls : components) {
                if (cls != null) fw.write(cls.getName() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to write metadata cache: " + e);
        }
    }

    public static Set<Class<?>> load(String... basePackages) {
        File f = new File(CACHE_FILE);
        if (!f.exists()) return Collections.emptySet();
        Set<String> filters = Arrays.stream(basePackages == null ? new String[0] : basePackages)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Predicate<String> filter = filters.isEmpty()
                ? name -> true
                : name -> filters.stream().anyMatch(name::startsWith);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            return br.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(filter)
                    .map(MetadataCache::loadClass)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    private static Class<?> loadClass(String name) {
        try { return Class.forName(name); }
        catch (ClassNotFoundException e) { return null; }
    }
}
