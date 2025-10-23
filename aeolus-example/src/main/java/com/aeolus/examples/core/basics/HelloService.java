package com.aeolus.examples.core.basics;

import com.aeolus.core.di.annotations.Component;

@Component
public class HelloService implements BasicService {
    public void sayHello() {
        System.out.println("Hello from Aeolus DI!");
    }
}
