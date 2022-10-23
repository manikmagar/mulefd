package com.javastreets.mulefd.drawings;

public class DrawingException extends RuntimeException {

  public DrawingException(String message) {
    super(message);
  }

  public DrawingException(String message, Throwable cause) {
    super(message, cause);
  }
}
