package com.aeolus.core.di;

import com.aeolus.core.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RecordingLogger implements Logger {
    private final String id;
    private final List<String> traceMessages = new ArrayList<>();
    private final List<String> infoMessages = new ArrayList<>();
    private final List<String> warnMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    RecordingLogger(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }

    List<String> infoMessages() {
        return Collections.unmodifiableList(infoMessages);
    }

    @Override
    public void trace(String msg, Object... args) {
        traceMessages.add(format(msg, args));
    }

    @Override
    public void info(String msg, Object... args) {
        infoMessages.add(format(msg, args));
    }

    @Override
    public void warn(String msg, Object... args) {
        warnMessages.add(format(msg, args));
    }

    @Override
    public void error(String msg, Object... args) {
        errorMessages.add(format(msg, args));
    }

    private String format(String msg, Object... args) {
        return (args == null || args.length == 0) ? msg : String.format(msg, args);
    }
}
