package com.aeolus.examples.core.basics;

import com.aeolus.core.di.Container;

public class BasicExample {
    public static void run() {
        Container container = Container.builder()
                .scan("com.aeolus.examples.core.basics")
                .build();

        BasicService svc = container.get(BasicService.class);
        svc.sayHello();
    }
}
