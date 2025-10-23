package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Component;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Component
@Singleton
public class ServiceB {
    @Inject ServiceA a;
}
