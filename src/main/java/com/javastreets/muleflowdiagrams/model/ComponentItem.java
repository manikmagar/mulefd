package com.javastreets.muleflowdiagrams.model;

public class ComponentItem {
  private String prefix;
  private String operation;
  private boolean source;
  private String configAttributeName;
  private String pathAttributeName;

  public String getPathAttributeName() {
    return pathAttributeName;
  }

  public void setPathAttributeName(String pathAttributeName) {
    this.pathAttributeName = pathAttributeName;
  }

  public String getConfigAttributeName() {
    return configAttributeName;
  }

  public void setConfigAttributeName(String configAttributeName) {
    this.configAttributeName = configAttributeName;
  }

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

  public String qualifiedName() {
    return prefix + ":" + operation;
  }

}
