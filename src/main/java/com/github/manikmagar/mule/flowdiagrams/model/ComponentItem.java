package com.github.manikmagar.mule.flowdiagrams.model;

public class ComponentItem {
  private String prefix;
  private String operation;
  private boolean source;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  private String path;

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public boolean isSource() {
    return source;
  }

  public void setSource(boolean source) {
    this.source = source;
  }

}
