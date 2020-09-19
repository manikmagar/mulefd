package com.javastreets.mulefd.util;

public class FileUtil {
  private FileUtil() {}

  public static String sanitizeFilename(String filename) {
    return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
  }
}
