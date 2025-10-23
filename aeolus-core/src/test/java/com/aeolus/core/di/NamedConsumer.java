package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Component;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Component
class NamedConsumer {

    private final NamedDummyWithDeps service;

    @Inject
    NamedConsumer(@Named("namedDummyWithDeps") NamedDummyWithDeps service) {
        this.service = service;
    }

    String call() {
        return service.ping();
    }
}
