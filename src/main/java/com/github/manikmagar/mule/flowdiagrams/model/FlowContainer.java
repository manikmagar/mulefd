package com.github.manikmagar.mule.flowdiagrams.model;

public class FlowContainer extends Container {

  public FlowContainer(String type, String name) {
    super(type, name);
  }

  public String qualifiedName() {
    return isSubflow() ? "sub-flow:" + getName() : "flow:" + getName();
  }

  public boolean isSubflow() {
    return getType().equalsIgnoreCase("sub-flow");
  }

}
