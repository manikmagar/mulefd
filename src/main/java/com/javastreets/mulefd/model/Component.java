package com.javastreets.mulefd.model;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Component))
      return false;

    Component component = (Component) o;

    if (!getName().equals(component.getName()))
      return false;
    if (!getType().equals(component.getType()))
      return false;
    if (getAttributes() != null ? !getAttributes().equals(component.getAttributes())
        : component.getAttributes() != null)
      return false;
    return getMuleVersion() != null ? getMuleVersion().equals(component.getMuleVersion())
        : component.getMuleVersion() == null;
  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + getType().hashCode();
    result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
    result = 31 * result + (getMuleVersion() != null ? getMuleVersion().hashCode() : 0);
    return result;
  }
}
