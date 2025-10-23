package com.aeolus.core.di;

import com.aeolus.core.di.exceptions.CircularDependencyException;
import com.aeolus.core.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {

    @Test
    void testComponentScanAndConfiguration() {
        Container container = Container.builder()
                .scan("com.aeolus.core.di")
                .build();

        DummyService svc = container.get(DummyService.class);
        assertEquals("primary", ((RecordingLogger) svc.logger()).id());
        assertEquals("pong", svc.ping());
    }

    @Test
    void testCircularDependencyDetection() {
        Container container = Container.builder()
                .scan("com.aeolus.core.di")
                .build();

        assertThrows(CircularDependencyException.class,
                () -> container.get(ServiceA.class),
                "Circular dependency should throw");
    }

    @Test
    void testLoggerInjection() {
        Container container = Container.builder()
                .scan("com.aeolus.core.di")
                .build();

        Logger logger = container.get(Logger.class);
        assertNotNull(logger);
        assertTrue(logger instanceof RecordingLogger);
        assertEquals("default", ((RecordingLogger) logger).id());
    }

    @Test
    void testNamedComponentInjectionWithDependencies() {
        Container container = Container.builder()
                .scan("com.aeolus.core.di")
                .build();

        NamedConsumer consumer = container.get(NamedConsumer.class);
        assertEquals("named-pong", consumer.call());
        NamedDummyWithDeps resolvedByName = (NamedDummyWithDeps) container.getByName("namedDummyWithDeps");
        assertNotNull(resolvedByName);
    }

    @Test
    void testThreadScopeIsPerThread() throws ExecutionException, InterruptedException {
        Container container = Container.builder()
                .scan("com.aeolus.core.di")
                .build();

        ThreadScopedComponent mainThreadInstance = container.get(ThreadScopedComponent.class);
        ThreadScopedComponent sameThread = container.get(ThreadScopedComponent.class);
        assertSame(mainThreadInstance, sameThread);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ThreadScopedComponent> future = executor.submit(() -> container.get(ThreadScopedComponent.class));
            ThreadScopedComponent otherThreadInstance = future.get();
            assertNotSame(mainThreadInstance, otherThreadInstance);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testBuilderHonorsCustomLogger() {
        RecordingLogger recordingLogger = new RecordingLogger("builder");
        Container.builder()
                .logger(recordingLogger)
                .scan("com.aeolus.core.di")
                .build();

        assertTrue(recordingLogger.infoMessages().stream()
                .anyMatch(msg -> msg.contains("Scanned packages") || msg.contains("Loaded")));
    }
}
