package com.aeolus.core.di.exceptions;

public class CircularDependencyException extends AeolusException {
    public CircularDependencyException(String msg) { super(msg); }
}
