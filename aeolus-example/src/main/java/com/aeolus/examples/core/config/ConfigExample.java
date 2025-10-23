package com.aeolus.examples.core.config;

import com.aeolus.core.di.Container;

public class ConfigExample {
    public static void run() {
        Container c = Container.builder()
                .scan("com.aeolus.example.config")
                .loadProperties("application.properties")
                .build();

        DbConfig cfg = c.get(DbConfig.class);
        System.out.println(cfg);
    }
}
