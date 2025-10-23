package com.aeolus.examples.core.resource;

import com.aeolus.core.di.annotations.Component;
import jakarta.annotation.Resource;

@Component
public class ResourceExample {

    @Resource(name = "service.env")
    private String env;

    public void printEnv() {
        System.out.println("Current environment: " + env);
    }
}
