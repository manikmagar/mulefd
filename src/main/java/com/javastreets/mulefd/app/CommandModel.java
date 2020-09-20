package com.javastreets.mulefd.app;


import java.nio.file.Files;
import java.nio.file.Path;

import com.javastreets.mulefd.drawings.DiagramType;
import com.javastreets.mulefd.util.Validations;


public class CommandModel {

  private Path sourcePath;

  private Path targetPath;

  private DiagramType diagramType;

  private String outputFilename;

  private String flowName;

  private boolean generateSingles;

  private Integer muleVersion = 4;

  public String getFlowName() {
    return flowName;
  }

  public void setFlowName(String flowName) {
    this.flowName = flowName;
  }

  public Integer getMuleVersion() {
    return muleVersion;
  }

  public void setMuleVersion(Integer muleVersion) {
    this.muleVersion = muleVersion;
  }

  public String getOutputFilename() {
    return outputFilename;
  }

  public void setOutputFilename(String outputFilename) {
    this.outputFilename = outputFilename;
  }

  public Path getTargetPath() {
    return targetPath;
  }

  public void setTargetPath(Path targetPath) {
    this.targetPath = targetPath;
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

  public boolean isGenerateSingles() {
    return generateSingles;
  }

  public void setGenerateSingles(boolean generateSingles) {
    this.generateSingles = generateSingles;
  }
}
