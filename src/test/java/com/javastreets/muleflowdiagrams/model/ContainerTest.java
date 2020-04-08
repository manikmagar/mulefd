package com.javastreets.muleflowdiagrams.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContainerTest {

  @Test
  @DisplayName("Container allows adding a component")
  void containerComponents() {
    Container container = new Container("flow", "test-flow");
    container.addComponent(new MuleComponent("sub-flow", "test-sub-flow"));

    assertThat(container.getComponents()).hasSize(1).satisfies(muleComponent -> {
      assertThat(muleComponent).extracting(MuleComponent::getType, MuleComponent::getName)
          .contains(new Tuple("sub-flow", "test-sub-flow"));
    });
  }

  @Test
  @DisplayName("Container does not allow adding null component")
  void containerNullComponents() {
    Container container = new Container("flow", "test-flow");
    NullPointerException exception =
        catchThrowableOfType(() -> container.addComponent(null), NullPointerException.class);
    assertThat(exception).isNotNull();
  }

  @Test
  @DisplayName("Container extends Component")
  void parentType() {
    Container container = new Container("flow", "test-flow");
    assertThat(container).isInstanceOf(Component.class);
  }

}
