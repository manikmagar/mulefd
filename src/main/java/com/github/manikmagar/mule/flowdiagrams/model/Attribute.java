package com.github.manikmagar.mule.flowdiagrams.model;

public class Attribute<K, V> {
  private K name;
  private V value;

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public K getName() {
    return name;
  }

  public void setName(K name) {
    this.name = name;
  }

  public static <K, V> Attribute with(K name, V value) {
    Attribute<K, V> attr = new Attribute<>();
    attr.setName(name);
    attr.setValue(value);
    return attr;
  }
}
