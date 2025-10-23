package com.aeolus.core.logging;

public interface Logger {
  void trace(String msg, Object... args);
  void info(String msg, Object... args);
  void warn(String msg, Object... args);
  void error(String msg, Object... args);
}