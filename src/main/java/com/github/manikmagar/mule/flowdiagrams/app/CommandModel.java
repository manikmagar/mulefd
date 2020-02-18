package com.github.manikmagar.mule.flowdiagrams.app;

import com.github.manikmagar.mule.flowdiagrams.drawings.DiagramType;

import java.nio.file.Path;

public class CommandModel {

  private Path sourcePath;

  private Path resultPath;

  private DiagramType diagramType;

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
