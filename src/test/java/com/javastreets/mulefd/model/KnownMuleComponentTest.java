package com.javastreets.mulefd.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KnownMuleComponentTest {

  @Test
  @DisplayName("Qualified name with no path attribute")
  void qualifiedName_no_path() {
    KnownMuleComponent component = new KnownMuleComponent("prefix", "operation");
    assertThat(component.qualifiedName()).isEqualTo("prefix:operation");
  }

  @Test
  @DisplayName("Qualified name with null path value")
  void qualifiedName_null_path() {
    KnownMuleComponent component = new KnownMuleComponent("prefix", "operation");
    component.setPath(Attribute.with("tag", null));
    assertThat(component.qualifiedName()).isEqualTo("prefix:operation");
  }

  @Test
  @DisplayName("Qualified name with path value")
  void qualifiedName_some_path() {
    KnownMuleComponent component = new KnownMuleComponent("prefix", "operation");
    component.setPath(Attribute.with("tag", "pathValue"));
    assertThat(component.qualifiedName()).isEqualTo("prefix:operation:pathValue");
  }
}
