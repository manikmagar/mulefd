package com.javastreets.mulefd.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class GraphTest {

  @TempDir
  File tempDir;

  @ParameterizedTest
  @MethodSource("componentConfigProvider")
  void componentRendering(String filepath, String filename) throws Exception {
    String[] args =
        new String[] {"graph", "--out", filename, "-t", tempDir.getAbsolutePath(), filepath};
    MuleFD.getCommandLine().execute(args);
    String outputFilename = filename + ".png";
    assertThat(Paths.get(tempDir.getAbsolutePath(), outputFilename)).exists();
    // .hasSameBinaryContentAs(Paths.get(toAbsolutePath("./renderer/component-configs/"),
    // outputFilename)); // This fails due to time stamp difference
  }

  static Stream<Arguments> componentConfigProvider() throws IOException {
    return Files.list(Paths.get("src/test/resources/renderer/component-configs/"))
        .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".xml"))
        .map(path -> Arguments.of(path.toAbsolutePath().toString(), path.getFileName().toString()));
  }

  @Test
  void renderFileWithBadFlowNamesForFiles() throws Exception {
    Path filePath = Paths.get("src/test/resources/test-api.xml");
    String[] args = new String[] {"graph", "--out", filePath.getFileName().toString(), "-t",
        tempDir.getAbsolutePath(), "-gs", filePath.toAbsolutePath().toString()};
    MuleFD.getCommandLine().execute(args);
    String outputFilename = filePath.getFileName().toString() + ".png";
    assertThat(Paths.get(tempDir.getAbsolutePath(), outputFilename)).exists();
    // .hasSameBinaryContentAs(Paths.get(toAbsolutePath("./renderer/component-configs/"),
    // outputFilename)); // This fails due to time stamp difference
  }

  @Test
  @DisplayName("Render Mule 3 example config")
  void mule3Rendering() throws Exception {
    String[] args = new String[] {"graph", "-t", tempDir.toString(),
        Paths.get("src/test/resources/renderer/mule3-example").toAbsolutePath().toString()};
    MuleFD.getCommandLine().execute(args);
    String outputFilename = "mule-diagram.png";
    assertThat(Paths.get(tempDir.getAbsolutePath(), outputFilename)).exists();
  }

}
