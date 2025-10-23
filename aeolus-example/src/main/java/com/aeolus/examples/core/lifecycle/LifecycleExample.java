package com.aeolus.examples.core.lifecycle;

import com.aeolus.core.di.Container;

public class LifecycleExample {
    public static void run() {
        try (Container container = Container.builder()
                .scan("com.aeolus.example.lifecycle")
                .build()) {

            Database db = container.get(Database.class);
        } // triggers @PreDestroy automatically
    }
}
