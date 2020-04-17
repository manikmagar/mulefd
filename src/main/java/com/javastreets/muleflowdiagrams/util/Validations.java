package com.javastreets.muleflowdiagrams.util;

public class Validations {

  private Validations() {

  }

  /**
   * Validates that passed value is true.
   * 
   * @param value to be checked
   * @param message to be thrown if value is false
   * @throws IllegalArgumentException
   * @return true
   */
  public static boolean requireTrue(boolean value, String message) {
    if (!value) {
      throw new IllegalArgumentException(message);
    }
    return true;
  }

  public static boolean requireTrue(boolean value) {
    return requireTrue(value, "Value must be true");
  }

  /**
   * Validates that passed value is false.
   * 
   * @param value to be checked
   * @param message to be thrown if value is true
   * @throws IllegalArgumentException
   * @return true
   */
  public static boolean requireFalse(boolean value, String message) {
    if (value) {
      throw new IllegalArgumentException(message);
    }
    return false;
  }

  public static boolean requireFalse(boolean value) {
    return requireFalse(value, "Value must be false");
  }
}
