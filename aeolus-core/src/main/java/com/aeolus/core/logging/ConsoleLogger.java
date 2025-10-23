package com.aeolus.core.logging;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class ConsoleLogger implements Logger {
  private static final DateTimeFormatter TS =
          DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

  private String fmt(String lvl, String msg, Object... args) {
    String t = (args.length == 0) ? msg : String.format(msg, args);
    return String.format("%s [%s] %s", TS.format(Instant.now()), lvl, t);
  }

  @Override public void trace(String m, Object... a){ System.out.println(fmt("TRACE", m, a)); }
  @Override public void info(String m, Object... a){ System.out.println(fmt("INFO", m, a)); }
  @Override public void warn(String m, Object... a){ System.out.println(fmt("WARN", m, a)); }
  @Override public void error(String m, Object... a){ System.err.println(fmt("ERROR", m, a)); }
}
