package com.javastreets.mulefd;

public class MuleFDException extends RuntimeException {

  public MuleFDException(String message) {
    super(message);
  }

  public MuleFDException(String message, Throwable cause) {
    super(message, cause);
  }
}
