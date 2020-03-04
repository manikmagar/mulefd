package com.github.manikmagar.mule.flowdiagrams.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ComponentTest {

  class TestComponent extends Component {

    public TestComponent(String type, String name) {
      super(type, name);
    }
  }

  @Test
  @DisplayName("Null name is not allowed")
  void constructorNullNameNotAllowed() {
    NullPointerException npe =
        catchThrowableOfType(() -> new TestComponent("flow", null), NullPointerException.class);
    assertThat(npe).isNotNull();
  }

  @Test
  @DisplayName("Null type is not allowed")
  void constructorNullTypeNotAllowed() {
    NullPointerException npe = catchThrowableOfType(() -> new TestComponent(null, "test-flow"),
        NullPointerException.class);
    assertThat(npe).isNotNull();
  }

  @Test
  @DisplayName("Component with attribute")
  void attributes() {
    TestComponent component = new TestComponent("flow", "test-flow");
    component.addAttribute("config-ref", "test-config");
    assertThat(component.getAttributes()).containsEntry("config-ref", "test-config");
  }

  @Test
  @DisplayName("Get attributes must return unmodifiable map")
  void unmodifiableAttributesMap() {
    TestComponent component = new TestComponent("flow", "test-flow");
    component.addAttribute("config-ref", "test-config");
    UnsupportedOperationException thrown = catchThrowableOfType(
        () -> component.getAttributes().put("test", "test"), UnsupportedOperationException.class);
    assertThat(thrown).isNotNull();
  }

  @Test
  @DisplayName("Component is a flow kind when type is flow")
  void isFlowKindForFlow() {
    TestComponent component = new TestComponent("flow", "test-flow");
    assertThat(component.isFlowKind()).isTrue();
  }

  @Test
  @DisplayName("Component is a flow kind when type is sub-flow")
  void isFlowKindForSubFlow() {
    TestComponent component = new TestComponent("sub-flow", "test-flow");
    assertThat(component.isFlowKind()).isTrue();
  }

  @Test
  @DisplayName("Component is a flow")
  void isaFlow() {
    TestComponent component = new TestComponent("flow", "test-flow");
    assertThat(component.isaFlow()).as("Component is a flow").isTrue();
    assertThat(component.isaSubFlow()).as("Component is a sub-flow").isFalse();
  }

  @Test
  @DisplayName("Component is a sub-flow")
  void isaSubFlow() {
    TestComponent component = new TestComponent("sub-flow", "test-flow");
    assertThat(component.isaFlow()).as("Component is a flow").isFalse();
    assertThat(component.isaSubFlow()).as("Component is a sub-flow").isTrue();
  }

  @Test
  @DisplayName("Test name, type, and qualifier")
  void componentQualifiers() {
    TestComponent component = new TestComponent("flow", "test-flow");
    assertThat(component)
        .extracting(Component::getType, Component::getName, Component::qualifiedName)
        .containsExactly("flow", "test-flow", "flow:test-flow");
  }
}
