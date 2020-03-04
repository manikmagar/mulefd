package com.github.manikmagar.mule.flowdiagrams.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;


public abstract class Component {
  private String name;
  private String type;
  private Map<String, Object> attributes = new HashMap<>();
  private Integer muleVersion;

  public Integer getMuleVersion() {
    return muleVersion;
  }

  public void setMuleVersion(Integer muleVersion) {
    this.muleVersion = muleVersion;
  }

  public Component(String type, String name) {
    this.name = requireNonNull(name, "Component name must not be null");
    this.type = requireNonNull(type, "Component type must not be null");
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

  public boolean isaSubFlow() {
    return "sub-flow".equalsIgnoreCase(getType());
  }
}
