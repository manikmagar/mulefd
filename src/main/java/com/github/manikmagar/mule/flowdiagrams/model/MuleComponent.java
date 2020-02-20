package com.github.manikmagar.mule.flowdiagrams.model;

public class MuleComponent extends Component {
  private boolean async;
  private boolean source;
  private Attribute<String, String> configRef;
  private Attribute<String, String> path;


  public MuleComponent(String type, String name) {
    super(type, name);
  }

  public boolean isaFlowRef() {
    return this.getType().equalsIgnoreCase("flow-ref");
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public boolean isSource() {
    return source;
  }

  public void setSource(boolean source) {
    this.source = source;
  }

  public Attribute<String, String> getConfigRef() {
    return configRef;
  }

  public void setConfigRef(Attribute<String, String> configRef) {
    this.configRef = configRef;
  }

  public Attribute<String, String> getPath() {
    return path;
  }

  public void setPath(Attribute<String, String> path) {
    this.path = path;
  }
}
