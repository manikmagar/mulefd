package com.javastreets.muleflowdiagrams.drawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import com.javastreets.muleflowdiagrams.model.Component;
import com.javastreets.muleflowdiagrams.model.FlowContainer;
import com.javastreets.muleflowdiagrams.model.MuleComponent;

import guru.nidi.graphviz.model.MutableGraph;
import io.github.netmikey.logunit.api.LogCapturer;

class GraphDiagramTest {

  @TempDir
  File tempDir;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(GraphDiagram.class, Level.DEBUG);

  private static final long MEGABYTE = 1024L * 1024L;

  public static long bytesToMegabytes(long bytes) {
    return bytes / MEGABYTE;
  }

  @Test
  void draw() {
    // Get the Java runtime
    Runtime runtime = Runtime.getRuntime();
    // Run the garbage collector
    runtime.gc();
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
    runtime.gc();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory is bytes: " + memory);
    System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));
    assertThat(output).exists();
    logs.assertContains(
        "Detected a possible self loop in sub-flow test-sub-flow. Skipping flow-ref processing.");
  }

  @Test
  void drawASingleFlow() {
    // Get the Java runtime
    Runtime runtime = Runtime.getRuntime();
    // Run the garbage collector
    runtime.gc();
    File output = new File(tempDir, "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    context.setFlowName("test-flow-2");
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow-1");
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");

    // Add reference to same sub-flow, resulting loop
    subflow.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));

    FlowContainer flowContainer2 = new FlowContainer("flow", "test-flow-2");
    flowContainer2.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));

    context.setComponents(Arrays.asList(flowContainer, subflow, flowContainer2));
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    graphDiagram.draw(context);
    runtime.gc();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory is bytes: " + memory);
    System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));
    assertThat(output).exists();
    ArgumentCaptor<Component> compArg = ArgumentCaptor.forClass(Component.class);
    Mockito.verify(graphDiagram, Mockito.times(2)).processComponent(compArg.capture(),
        any(MutableGraph.class), eq(context), anyMap(), anyList());
    assertThat(compArg.getAllValues()).containsExactly(flowContainer2, subflow);
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
