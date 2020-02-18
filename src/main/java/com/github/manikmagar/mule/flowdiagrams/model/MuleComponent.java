package com.github.manikmagar.mule.flowdiagrams.model;

public class MuleComponent extends Component {
  private boolean async;

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

}
