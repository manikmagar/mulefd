package com.github.manikmagar.mule.flowdiagrams.drawings;

import com.github.manikmagar.mule.flowdiagrams.model.FlowContainer;
import com.github.manikmagar.mule.flowdiagrams.model.MuleComponent;
import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDiagramTest {

  @TempDir
  File tempDir;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(GraphDiagram.class, Level.DEBUG);


  @Test
  void draw() {
    File output = new File(tempDir, "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow");
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");

    // Add reference to same sub-flow, resulting loop
    subflow.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));

    context.setComponents(Arrays.asList(flowContainer, subflow));
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = new GraphDiagram();
    graphDiagram.draw(context);

    assertThat(output).exists();
    logs.assertContains(
        "Detected a possible self loop in sub-flow test-sub-flow. Skipping flow-ref processing.");
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
