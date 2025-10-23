package com.aeolus.examples.core.processor;

import com.aeolus.core.di.Container;
import com.aeolus.examples.core.basics.HelloService;

public class ProcessorExample {
    public static void run() {
        Container c = Container.builder()
                .scan("com.aeolus.example.basics")
                .addProcessor(new LoggingBeanProcessor())
                .build();

        c.get(HelloService.class).sayHello();
    }
}
