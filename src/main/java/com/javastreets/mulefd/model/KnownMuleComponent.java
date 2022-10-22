package com.javastreets.mulefd.model;

/**
 * {@link Component} implementation for components configured using known components csv file. Helps
 * to differentiate default components and csv components for processing.
 */
public class KnownMuleComponent extends MuleComponent {
  public KnownMuleComponent(String type, String name) {
    super(type, name);
  }
}
