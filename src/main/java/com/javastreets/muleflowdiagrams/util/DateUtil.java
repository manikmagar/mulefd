package com.javastreets.muleflowdiagrams.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

  private DateUtil() {

  }

  public static String now() {
    return now("dd-MMM-yyyy HH:mm:ss.SSS");
  }

  public static String now(String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    LocalDateTime date = LocalDateTime.now();
    return date.format(formatter);
  }


}
