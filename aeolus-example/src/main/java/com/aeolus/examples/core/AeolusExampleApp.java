package com.aeolus.examples.core;

import com.aeolus.examples.core.basics.BasicExample;
import com.aeolus.examples.core.config.ConfigExample;
import com.aeolus.examples.core.introspection.StatsExample;
import com.aeolus.examples.core.lifecycle.LifecycleExample;
import com.aeolus.examples.core.processor.ProcessorExample;
import com.aeolus.examples.core.resource.ResourceExample;
import com.aeolus.examples.core.scope.ScopeExample;

public class AeolusExampleApp {
    public static void main(String[] args) {
        System.out.println("---- Basic Injection ----");
        BasicExample.run();

        System.out.println("\n---- Lifecycle Hooks ----");
        LifecycleExample.run();

        System.out.println("\n---- Scope Example ----");
        ScopeExample.run();

        System.out.println("\n---- Bean Processor Example ----");
        ProcessorExample.run();

        System.out.println("\n---- Config Binding Example ----");
        ConfigExample.run();

        System.out.println("\n---- Resource Example ----");
        new ResourceExample().printEnv();

        System.out.println("\n---- Stats Example ----");
        StatsExample.run();

        System.out.println("\nAll Aeolus examples executed successfully ✅");
    }
}
