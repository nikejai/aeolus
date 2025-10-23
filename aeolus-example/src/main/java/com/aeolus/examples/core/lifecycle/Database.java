package com.aeolus.examples.core.lifecycle;

import com.aeolus.core.di.annotations.Component;
import jakarta.annotation.*;

@Component
public class Database {

    @PostConstruct
    void init() {
        System.out.println("[Lifecycle] Database connection established.");
    }

    @PreDestroy
    void shutdown() {
        System.out.println("[Lifecycle] Database connection closed.");
    }
}
