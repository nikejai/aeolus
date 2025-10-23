package com.aeolus.examples.core.scope;

import com.aeolus.core.di.Container;

public class ScopeExample {
    public static void run() {
        Container c = Container.builder().scan("com.aeolus.examples.core").build();
        ScopedService a = c.get(ScopedService.class);
        ScopedService b = c.get(ScopedService.class);
        a.ping(); b.ping();
    }
}
