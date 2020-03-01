package com.github.manikmagar.mule.flowdiagrams.app;

import com.github.manikmagar.mule.flowdiagrams.drawings.DiagramType;
import picocli.CommandLine;

import java.nio.file.Path;

public class CommandModel {

  @CommandLine.Parameters(paramLabel = "Source Path", description = "Directory path where mule configuration files exist")
  private Path sourcePath;

  @CommandLine.Option(names = {"-o", "--out"}, paramLabel = "Output path", description = "Output path where resulted diagram file will be generated.")
  private Path resultPath;

  @CommandLine.Option(names = {"-d", "--diagram"}, paramLabel = "Diagram Type", defaultValue = "GRAPH")
  private DiagramType diagramType;

  @CommandLine.Option(names = {"-outFilename"}, paramLabel = "Output filename", defaultValue = "mule-diagram")
  private String outputFilename;

  public String getOutputFilename() {
    return outputFilename;
  }

  public void setOutputFilename(String outputFilename) {
    this.outputFilename = outputFilename;
  }

  public Path getResultPath() {
    return resultPath;
  }

  public void setResultPath(Path resultPath) {
    this.resultPath = resultPath;
  }

  public Path getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath(Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  public DiagramType getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(DiagramType diagramType) {
    this.diagramType = diagramType;
  }
}
