package com.javastreets.mulefd.app;

public class DrawingException extends RuntimeException {

  public DrawingException(String message) {
    super(message);
  }

  public DrawingException(String message, Throwable cause) {
    super(message, cause);
  }
}
