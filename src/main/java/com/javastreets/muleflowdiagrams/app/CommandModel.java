package com.javastreets.muleflowdiagrams.app;


import java.nio.file.Files;
import java.nio.file.Path;

import com.javastreets.muleflowdiagrams.drawings.DiagramType;
import com.javastreets.muleflowdiagrams.util.Validations;


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
    Validations.requireTrue(Files.exists(sourcePath), "Source path must be a valid directory");
    this.sourcePath = sourcePath;
  }

  public DiagramType getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(DiagramType diagramType) {
    this.diagramType = diagramType;
  }
}
