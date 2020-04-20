package com.javastreets.muleflowdiagrams.drawings;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.javastreets.muleflowdiagrams.model.Component;
import com.javastreets.muleflowdiagrams.model.ComponentItem;

public class DrawingContext {
  private List<Component> components;
  private DiagramType diagramType;
  private File outputFile;
  private Map<String, ComponentItem> knownComponents;
  private String flowName;

  public String getFlowName() {
    return flowName;
  }

  public void setFlowName(String flowName) {
    this.flowName = flowName;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public void setOutputFile(File outputFile) {
    this.outputFile = outputFile;
  }

  public List<Component> getComponents() {
    return components;
  }

  public void setComponents(List<Component> components) {
    this.components = components;
  }

  public DiagramType getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(DiagramType diagramType) {
    this.diagramType = diagramType;
  }

  public Map<String, ComponentItem> getKnownComponents() {
    return knownComponents;
  }

  public void setKnownComponents(Map<String, ComponentItem> knownComponents) {
    this.knownComponents = Collections.unmodifiableMap(knownComponents);
  }
}
