package com.github.manikmagar.mule.flowdiagrams.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;


public class Container extends Component {

  private List<MuleComponent> components = new ArrayList<>();

  public Container(String type, String name) {
    super(type, name);
  }

  public void addComponent(MuleComponent component) {
    this.components.add(requireNonNull(component, "Component cannot be null"));
  }

  public List<MuleComponent> getComponents() {
    return components;
  }

}
