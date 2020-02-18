package com.github.manikmagar.mule.flowdiagrams.drawings;

import com.github.manikmagar.mule.flowdiagrams.model.Component;
import com.github.manikmagar.mule.flowdiagrams.model.ComponentItem;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class DrawingContext {
  private List<Component> components;
  private DiagramType diagramType;
  private File outputFile;
  private List<ComponentItem> knownComponents;

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

  public List<ComponentItem> getKnownComponents() {
    return knownComponents;
  }

  public void setKnownComponents(List<ComponentItem> knownComponents) {
    this.knownComponents = Collections.unmodifiableList(knownComponents);
  }
}
