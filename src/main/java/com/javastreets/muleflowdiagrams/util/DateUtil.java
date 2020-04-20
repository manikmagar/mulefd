package com.javastreets.muleflowdiagrams.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

  private DateUtil() {

  }

  public static String now() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");
    LocalDateTime date = LocalDateTime.now();
    return date.format(formatter);
  }

}
