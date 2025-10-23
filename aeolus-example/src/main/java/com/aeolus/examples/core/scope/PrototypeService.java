package com.aeolus.examples.core.scope;

import com.aeolus.core.di.annotations.Component;
import com.aeolus.core.di.annotations.Scope;

@Component
@Scope("prototype")
public class PrototypeService implements ScopedService {
    @Override
    public void ping() {
        System.out.println("New instance: " + this.hashCode());
    }
}