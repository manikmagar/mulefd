package com.github.manikmagar.mule.flowdiagrams.model;

import java.util.ArrayList;
import java.util.List;

public class MuleFlow extends Component {
  private List<MuleComponent> components = new ArrayList<>();

  public MuleFlow(String type, String name) {
    super(type, name);
  }

  public MuleFlow(String name) {
    super("flow", name);
  }

  public String qualifiedName() {
    return isSubflow() ? "sub-flow:" + getName() : "flow:" + getName();
  }

  public void addComponent(MuleComponent component) {
    this.components.add(component);
  }

  public boolean isSubflow() {
    return getType().equalsIgnoreCase("sub-flow");
  }

  public List<MuleComponent> getComponents() {
    return components;
  }

}
