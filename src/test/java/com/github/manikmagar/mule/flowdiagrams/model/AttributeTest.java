package com.github.manikmagar.mule.flowdiagrams.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeTest {

  @Test
  @DisplayName("Attributes with same name-value are equal")
  void attributeEquals() {
    Attribute<String, String> attr1 = Attribute.with("test", "value");
    Attribute<String, String> attr2 = Attribute.with("test", "value");
    assertThat(attr1).isEqualTo(attr2);
  }

  @Test
  @DisplayName("Attributes with properties")
  void attributeProperties() {
    Attribute<String, String> attr = Attribute.with("test", "value");
    assertThat(attr).extracting(Attribute::getName, Attribute::getValue).containsExactly("test",
        "value");
  }

}
