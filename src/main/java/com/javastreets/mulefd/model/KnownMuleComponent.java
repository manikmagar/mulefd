package com.javastreets.mulefd.model;

/**
 * {@link Component} implementation for components configured using known components csv file. Helps
 * to differentiate default components and csv components for processing.
 */
public class KnownMuleComponent extends MuleComponent {
  public KnownMuleComponent(String type, String name) {
    super(type, name);
  }

  @Override
  public String qualifiedName() {
    String name = super.qualifiedName();
    if (getPath() != null && getPath().getValue() != null) {
      name = name + ":" + getPath().getValue();
    }
    return name;
  }
}
