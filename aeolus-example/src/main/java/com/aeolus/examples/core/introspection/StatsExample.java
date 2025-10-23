package com.aeolus.examples.core.introspection;

import com.aeolus.core.di.Container;
import com.aeolus.examples.core.basics.BasicService;

public class StatsExample {
    public static void run() {
        Container c = Container.builder()
                .scan("com.aeolus.examples.core")
                .build();
        c.get(BasicService.class);
        System.out.println("Container stats: " + c.stats());
    }
}
