package com.javastreets.mulefd.drawings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.javastreets.mulefd.model.Component;
import com.javastreets.mulefd.model.FlowContainer;

class DiagramTest {

  private class TestDiagram implements Diagram {

    @Override
    public boolean draw(DrawingContext drawingContext) {
      return false;
    }

    @Override
    public boolean supports(DiagramType diagramType) {
      return DiagramType.GRAPH.equals(diagramType);
    }

    @Override
    public String name() {
      return "TestDiagram";
    }
  }

  @Test
  @DisplayName("Validate diagram support")
  void supports() {
    TestDiagram diagram = new TestDiagram();
    assertThat(diagram.supports(DiagramType.GRAPH)).isTrue();
    assertThat(diagram.supports(DiagramType.SEQUENCE)).isFalse();
  }

  @Test
  @DisplayName("Returns diagram name")
  void name() {
    assertThat(new TestDiagram().name()).isEqualTo("TestDiagram");
  }

  @Test
  @DisplayName("Returns date")
  void getDate() {
    TestDiagram diagram = new TestDiagram();
    assertThat(diagram.getDate()).isNotNull();
  }

  @Test
  @DisplayName("Finds flow container by name")
  void targetFlowByName() {
    TestDiagram diagram = new TestDiagram();
    List<Component> flows = new ArrayList<>();
    flows.add(new FlowContainer("flow", "test-flow1"));
    flows.add(new FlowContainer("flow", "test-flow2"));
    Assertions.assertThat(diagram.targetFlowByName("test-flow2", flows))
        .isEqualTo(new FlowContainer("flow", "test-flow2"));
  }
}
