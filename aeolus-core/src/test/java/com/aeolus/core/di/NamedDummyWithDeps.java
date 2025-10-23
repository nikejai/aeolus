package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Component;
import com.aeolus.core.logging.Logger;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Component
@Named("namedDummyWithDeps")
class NamedDummyWithDeps {

    private final Logger logger;

    @Inject
    NamedDummyWithDeps(Logger logger) {
        this.logger = logger;
    }

    String ping() {
        logger.info("Ping from NamedDummyWithDeps");
        return "named-pong";
    }
}
