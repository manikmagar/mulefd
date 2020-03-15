package com.github.manikmagar.mule.flowdiagrams.util;

public class Validations {
  /**
   * Validates that passed value is true.
   * 
   * @param value to be checked
   * @param message to be thrown if value is false
   * @throws IllegalArgumentException
   * @return true
   */
  public static boolean requireTrue(boolean value, String message) {
    if (value == false) {
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
    if (value == true) {
      throw new IllegalArgumentException(message);
    }
    return false;
  }

  public static boolean requireFalse(boolean value) {
    return requireFalse(value, "Value must be false");
  }
}
