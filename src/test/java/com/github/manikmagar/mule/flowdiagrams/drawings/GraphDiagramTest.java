package com.github.manikmagar.mule.flowdiagrams.drawings;

import com.github.manikmagar.mule.flowdiagrams.model.FlowContainer;
import com.github.manikmagar.mule.flowdiagrams.model.MuleComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDiagramTest {

  @TempDir
  File tempDir;

  @Test
  void draw() {
    File output = new File(tempDir, "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow");
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");
    context.setComponents(Arrays.asList(flowContainer, subflow));
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = new GraphDiagram();
    graphDiagram.draw(context);
    System.out.println(output);
    assertThat(output).exists();
  }

  @Test
  void supports() {
    GraphDiagram graphDiagram = new GraphDiagram();
    assertThat(graphDiagram.supports(DiagramType.GRAPH)).isEqualTo(true);
  }

  @Test
  void name() {
    GraphDiagram graphDiagram = new GraphDiagram();
    assertThat(graphDiagram.name()).isEqualTo("Graph");
  }
}
