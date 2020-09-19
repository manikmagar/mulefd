package com.javastreets.mulefd.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FileUtilTest {

  @ParameterizedTest
  @MethodSource("filenameProvider")
  public void sanitizeFilenameTest(String filename, String expected) {
    assertThat(FileUtil.sanitizeFilename(filename)).isEqualTo(expected);
  }


  static Stream<Arguments> filenameProvider() {
    return Stream.of(arguments("abcd1234.png", "abcd1234.png"),
        arguments("abcd:1234.png", "abcd_1234.png"), arguments("abcd;1234.png", "abcd_1234.png"),
        arguments("abcd$1234.png", "abcd_1234.png"));
  }

}
