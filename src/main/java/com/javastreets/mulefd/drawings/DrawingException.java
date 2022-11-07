package com.javastreets.mulefd.drawings;

import com.javastreets.mulefd.MuleFDException;

public class DrawingException extends MuleFDException {

  public DrawingException(String message) {
    super(message);
  }

  public DrawingException(String message, Throwable cause) {
    super(message, cause);
  }
}
