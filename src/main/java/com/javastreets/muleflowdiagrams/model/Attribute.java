package com.javastreets.muleflowdiagrams.model;

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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Attribute<?, ?> attribute = (Attribute<?, ?>) o;

    if (!getName().equals(attribute.getName()))
      return false;
    return getValue() != null ? getValue().equals(attribute.getValue())
        : attribute.getValue() == null;
  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
    return result;
  }
}
