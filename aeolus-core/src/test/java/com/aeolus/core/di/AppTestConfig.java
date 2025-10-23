package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Bean;
import com.aeolus.core.di.annotations.Configuration;
import com.aeolus.core.logging.Logger;
import jakarta.inject.Named;

@Configuration
public class AppTestConfig {
    @Bean
    public Logger logger() {
        return new RecordingLogger("default");
    }

    @Bean
    @Named("primaryLogger")
    public Logger primaryLogger() {
        return new RecordingLogger("primary");
    }

    @Bean
    public DummyService dummyService(@Named("primaryLogger") Logger logger) {
        return new DummyService(logger);
    }
}
