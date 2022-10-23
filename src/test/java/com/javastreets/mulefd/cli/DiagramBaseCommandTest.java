package com.javastreets.mulefd.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.javastreets.mulefd.drawings.DiagramType;

import picocli.CommandLine;

class DiagramBaseCommandTest {

  @TempDir
  File tempDir;

  @Test
  @DisplayName("Command line arguments - only source as directory")
  void commandLineWithSourceDirectory() {
    String[] args = new String[] {tempDir.getAbsolutePath()};
    Graph application = new Graph();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    Path path = Paths.get(tempDir.getAbsolutePath());
    expectedModel.setSourcePath(path);
    expectedModel.setTargetPath(path);
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("mule-diagram.png");
    assertThat(model).usingRecursiveComparison().ignoringFieldsOfTypes(Configuration.class)
        .isEqualTo(expectedModel);

  }

  @Test
  @DisplayName("When parsing source file without parent folder, sets parent as target")
  void commandLineWithBareSourceFile() throws Exception {
    tempDir.createNewFile();
    Path sourceFile = Paths.get("db-config.xml");
    sourceFile.toFile().delete();
    Files.copy(Paths.get("src/test/resources/renderer/component-configs/db-config.xml"),
        sourceFile);
    String[] args = new String[] {sourceFile.toString()};
    Graph application = new Graph();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(sourceFile);
    expectedModel.setTargetPath(sourceFile.toAbsolutePath().getParent());
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("mule-diagram.png");
    expectedModel.setGenerateSingles(false);
    assertThat(model).usingRecursiveComparison().ignoringFieldsOfTypes(Configuration.class)
        .isEqualTo(expectedModel);
  }

  @Test
  @DisplayName("When parsing source file, sets parent as target")
  void commandLineWithSourceFile() throws Exception {
    tempDir.createNewFile();
    Path sourceFile = Paths.get(tempDir.getAbsolutePath(), "db-config.xml");
    Files.copy(Paths.get("src/test/resources/renderer/component-configs/db-config.xml"),
        sourceFile);
    String[] args = new String[] {sourceFile.toString()};
    Graph application = new Graph();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(sourceFile);
    expectedModel.setTargetPath(tempDir.toPath());
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("mule-diagram.png");
    expectedModel.setGenerateSingles(false);
    assertThat(model).usingRecursiveComparison().ignoringFieldsOfTypes(Configuration.class)
        .isEqualTo(expectedModel);
  }

  @Test
  @DisplayName("Parsing when all arguments are provided")
  void commandLineWithAllArgs() throws Exception {
    Path source = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "source"));
    Path target = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "target"));
    String[] args = new String[] {source.toString(), "-t", target.toString(), "-o", "test", "-fl",
        "test-flow", "-gs"};
    Graph application = new Graph();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(source.toAbsolutePath());
    expectedModel.setTargetPath(target.toAbsolutePath());
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("test.png");
    expectedModel.setFlowName("test-flow");
    expectedModel.setGenerateSingles(true);
    assertThat(model).usingRecursiveComparison().ignoringFieldsOfTypes(Configuration.class)
        .isEqualTo(expectedModel);
  }

  @Test
  @DisplayName("Parsing when flow name but not output filename uses flow name")
  void commandLineWithFlowNameWithoutOutputfilename() throws Exception {
    Path source = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "source"));
    Path target = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "target"));
    String[] args =
        new String[] {source.toString(), "-t", target.toString(), "--flowname", "test-flow"};
    Graph application = new Graph();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(source.toAbsolutePath());
    expectedModel.setTargetPath(target.toAbsolutePath());
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("test-flow.png");
    expectedModel.setFlowName("test-flow");
    assertThat(model).usingRecursiveComparison().ignoringFieldsOfTypes(Configuration.class)
        .isEqualTo(expectedModel);
  }

  @Test
  @DisplayName("Parsing arguments with alternate option names")
  void commandLineWithAllAlternateArgs() throws Exception {
    Path source = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "source"));
    Path target = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "target"));
    String[] args = new String[] {source.toString(), "--target", target.toString(), "--out", "test",
        "--flowname", "test-flow", "--genSingles"};
    Graph application = new Graph();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(source.toAbsolutePath());
    expectedModel.setTargetPath(target.toAbsolutePath());
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("test.png");
    expectedModel.setFlowName("test-flow");
    expectedModel.setGenerateSingles(true);
    assertThat(model).usingRecursiveComparison().ignoringFieldsOfTypes(Configuration.class)
        .isEqualTo(expectedModel);
  }
}
