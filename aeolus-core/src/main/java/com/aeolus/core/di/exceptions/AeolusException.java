package com.aeolus.core.di.exceptions;

public class AeolusException extends RuntimeException {
    public AeolusException(String msg) { super(msg); }
    public AeolusException(String msg, Throwable t) { super(msg, t); }
}
