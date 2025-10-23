package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Component;
import com.aeolus.core.logging.Logger;
import jakarta.inject.Inject;

@Component
public class DummyService {
    private final Logger logger;
    @Inject
    DummyService(Logger logger) { this.logger = logger; }
    String ping() { logger.info("Ping from DummyService"); return "pong"; }
    Logger logger() { return logger; }
}
