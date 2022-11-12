package com.javastreets.mulefd.model;

public class ComponentItem {
  private String prefix;
  private String operation;
  private boolean source;
  private String configAttributeName;
  private String pathAttributeName;
  private boolean async;

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

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public boolean hasWildcardOperation() {
    return "*".equals(operation);
  }
}
