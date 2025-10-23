package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Component;
import com.aeolus.core.di.annotations.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Recursively scans the classpath for annotated components and configuration classes.
 * Supports multiple base packages and nested subpackages.
 */
public final class ComponentScanner {

  private ComponentScanner() {}

  public static Set<Class<?>> scan(String... basePackages) {
    Set<Class<?>> result = new HashSet<>();
    for (String basePackage : basePackages) {
      result.addAll(scanSingle(basePackage));
    }
    return result;
  }

  private static Set<Class<?>> scanSingle(String basePackage) {
    Set<Class<?>> classes = new HashSet<>();
    String path = basePackage.replace('.', '/');
    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    try {
      Enumeration<URL> resources = cl.getResources(path);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        String protocol = resource.getProtocol();

        if ("file".equals(protocol)) {
          File dir = new File(resource.getFile());
          classes.addAll(scanDirectory(basePackage, dir));
        } else if ("jar".equals(protocol)) {
          classes.addAll(scanJar(resource, path));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to scan package: " + basePackage, e);
    }

    return classes;
  }

  private static Set<Class<?>> scanDirectory(String basePackage, File dir) {
    Set<Class<?>> classes = new HashSet<>();
    if (!dir.exists()) return classes;

    File[] files = dir.listFiles();
    if (files == null) return classes;

    for (File file : files) {
      if (file.isDirectory()) {
        // recursive descent into subpackages
        String subPackage = basePackage + "." + file.getName();
        classes.addAll(scanDirectory(subPackage, file));
      } else if (file.getName().endsWith(".class")) {
        String className = basePackage + '.' + file.getName().replace(".class", "");
        loadClassSafely(classes, className);
      }
    }
    return classes;
  }

  private static Set<Class<?>> scanJar(URL resource, String path) {
    Set<Class<?>> classes = new HashSet<>();
    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
    try (JarFile jarFile = new JarFile(jarPath)) {
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith(path) && name.endsWith(".class")) {
          String className = name.replace('/', '.').replace(".class", "");
          loadClassSafely(classes, className);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to scan jar for " + path, e);
    }
    return classes;
  }

  private static void loadClassSafely(Set<Class<?>> classes, String className) {
    try {
      Class<?> clazz = Class.forName(className);
      if (isAnnotated(clazz)) {
        classes.add(clazz);
      }
    } catch (Throwable ignored) {
      // skip classes that fail to load (optional dependencies etc.)
    }
  }

  private static boolean isAnnotated(Class<?> clazz) {
    return clazz.isAnnotationPresent(Component.class) ||
            clazz.isAnnotationPresent(Configuration.class);
  }
}
