package com.aeolus.core.di;

public interface BeanProcessor {
    Object postProcessBeforeInitialization(Object bean);
    Object postProcessAfterInitialization(Object bean);
}
