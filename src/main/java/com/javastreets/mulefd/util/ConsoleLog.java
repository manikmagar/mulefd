package com.javastreets.mulefd.util;


import static java.lang.System.err;
import static java.lang.System.out;

import java.io.PrintStream;

/**
 * Utility class to write logs to console.
 */
public class ConsoleLog {

  private static boolean verbose = false;

  private static PrintStream outStream = out;

  public static boolean isVerbose() {
    return verbose;
  }

  public static void setVerbose(boolean verbose) {
    ConsoleLog.verbose = verbose;
  }

  public static PrintStream getOutStream() {
    return outStream;
  }

  public static void setOutStream(PrintStream outStream) {
    ConsoleLog.outStream = outStream;
  }

  public static void defaultOutStream() {
    ConsoleLog.outStream = out;
  }

  private ConsoleLog() {

  }

  public static void info(String message, Object... args) {
    print(LEVEL.INFO, message, args);
  }

  public static void debug(String message, Object... args) {
    if (isVerbose())
      print(LEVEL.DEBUG, message, args);
  }

  public static void warn(String message, Object... args) {
    print(LEVEL.WARN, message, args);
  }

  public static void error(String message, Object... args) {
    print(LEVEL.ERROR, message, args, err);
  }

  private static void print(LEVEL level, String message, Object[] args) {
    print(level, message, args, getOutStream());
  }

  private static void print(LEVEL level, String message, Object[] args, PrintStream stream) {
    stream.printf(String.format("%s: %s", level, message), args);
    stream.println();
  }

  private enum LEVEL {
    INFO, DEBUG, WARN, ERROR
  }
}
