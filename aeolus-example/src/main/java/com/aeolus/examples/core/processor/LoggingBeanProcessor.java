package com.aeolus.examples.core.processor;

import com.aeolus.core.di.BeanProcessor;

public class LoggingBeanProcessor implements BeanProcessor {
    public Object postProcessBeforeInitialization(Object bean) {
        System.out.println("[BeanProcessor] Before: " + bean.getClass().getSimpleName());
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean) {
        System.out.println("[BeanProcessor] After: " + bean.getClass().getSimpleName());
        return bean;
    }
}
