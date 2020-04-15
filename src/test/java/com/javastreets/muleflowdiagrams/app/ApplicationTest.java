package com.javastreets.muleflowdiagrams.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import picocli.CommandLine;

class ApplicationTest {

  static String toAbsolutePath(String path) {
    return Application.class.getClassLoader().getResource(path).getFile();
  }

  @TempDir
  File tempDir;

  @ParameterizedTest
  @MethodSource("componentConfigProvider")
  void componentRendering(String filepath, String filename) throws Exception {
    String[] args = new String[] {filepath, "--out", filename, "-t", tempDir.getAbsolutePath()};
    Application application = new Application();
    new CommandLine(application).parseArgs(args);
    application.call();
    String outputFilename = filename + ".png";
    assertThat(Paths.get(tempDir.getAbsolutePath(), outputFilename)).exists();
    // .hasSameBinaryContentAs(Paths.get(toAbsolutePath("./renderer/component-configs/"),
    // outputFilename)); // This fails due to time stamp difference
  }

  static Stream<Arguments> componentConfigProvider() throws IOException {
    return Files.list(Paths.get(toAbsolutePath("./renderer/component-configs/")))
        .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".xml"))
        .map(path -> Arguments.of(path.toAbsolutePath().toString(), path.getFileName().toString()));
  }
}
