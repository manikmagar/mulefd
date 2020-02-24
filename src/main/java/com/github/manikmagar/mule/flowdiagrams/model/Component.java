package com.github.manikmagar.mule.flowdiagrams.model;

import java.util.*;


public class Component {
  private String name;
  private String type;
  private Map<String, Object> attributes = new HashMap<>();

  public Component(String type, String name) {
    Objects.requireNonNull(type, "Component type must not be null");
    Objects.requireNonNull(name, "Component name must not be null");
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public void addAttribute(String attrName, Object attrValue) {
    if (attrValue != null)
      this.attributes.putIfAbsent(attrName, attrValue);
  }

  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public boolean isFlowKind() {
    return Arrays.asList("flow", "sub-flow").contains(getType().toLowerCase());
  }

  public boolean isaFlow() {
    return "flow".equalsIgnoreCase(getType().toLowerCase());
  }

  public String qualifiedName() {
    return type + ":" + name;
  }

}
