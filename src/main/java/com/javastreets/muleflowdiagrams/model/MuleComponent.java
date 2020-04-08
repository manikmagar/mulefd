package com.javastreets.muleflowdiagrams.model;

public class MuleComponent extends Component {
  private boolean async;
  private boolean source;
  private Attribute<String, String> configRef;
  private Attribute<String, String> path;

  public MuleComponent(String type, String name) {
    super(type, name);
  }

  public boolean isaFlowRef() {
    return this.getType().equalsIgnoreCase("flow-ref");
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public boolean isSource() {
    return source;
  }

  public void setSource(boolean source) {
    this.source = source;
  }

  public Attribute<String, String> getConfigRef() {
    return configRef;
  }

  public void setConfigRef(Attribute<String, String> configRef) {
    this.configRef = configRef;
  }

  public Attribute<String, String> getPath() {
    return path;
  }

  public void setPath(Attribute<String, String> path) {
    this.path = path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof MuleComponent))
      return false;

    MuleComponent component = (MuleComponent) o;
    if (!super.equals(component))
      return false;
    if (isAsync() != component.isAsync())
      return false;
    if (isSource() != component.isSource())
      return false;
    if (getConfigRef() != null ? !getConfigRef().equals(component.getConfigRef())
        : component.getConfigRef() != null)
      return false;
    return getPath() != null ? getPath().equals(component.getPath()) : component.getPath() == null;
  }

  @Override
  public int hashCode() {
    int result = (isAsync() ? 1 : 0);
    result = 31 * result + (isSource() ? 1 : 0);
    result = 31 * result + (getConfigRef() != null ? getConfigRef().hashCode() : 0);
    result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
    return result;
  }
}
