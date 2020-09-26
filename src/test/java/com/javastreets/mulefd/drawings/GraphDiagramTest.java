package com.javastreets.mulefd.drawings;

import static guru.nidi.graphviz.attribute.Arrow.VEE;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.event.Level;

import com.javastreets.mulefd.DiagramRendererTestUtil;
import com.javastreets.mulefd.model.*;

import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.GraphAttr;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
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
    MuleComponent source = new MuleComponent("vm", "listener");
    source.setSource(true);
    source.setConfigRef(Attribute.with("config-ref", "test"));
    source.setPath(Attribute.with("queueName", "testVmQ"));
    flowContainer.addComponent(source);
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");

    // Add reference to same sub-flow, resulting loop
    subflow.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));

    context.setComponents(Arrays.asList(flowContainer, subflow));
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    graphDiagram.draw(context);
    runtime.gc();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory is bytes: " + memory);
    System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));
    assertThat(output).exists();
    verify(graphDiagram, Mockito.times(0)).writeFlowGraph(any(), any(), any());
    logs.assertContains(
        "Detected a possible self loop in sub-flow test-sub-flow. Skipping flow-ref processing.");
  }

  @Test
  @DisplayName("Validate generated graph when generated as JSON.")
  void drawToValidateGraph() throws Exception {

    File output = new File(".", "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow");

    MuleComponent source = new MuleComponent("vm", "listener");
    source.setSource(true);
    source.setConfigRef(Attribute.with("config-ref", "test"));
    source.setPath(Attribute.with("queueName", "testVmQ"));
    flowContainer.addComponent(source);
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");
    // Add reference to same sub-flow, resulting loop
    subflow.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    context.setComponents(Arrays.asList(flowContainer, subflow));

    ComponentItem item = new ComponentItem();
    item.setPrefix("vm");
    item.setOperation("listener");
    item.setSource(true);
    item.setConfigAttributeName("config-ref");
    item.setPathAttributeName("queueName");
    context.setKnownComponents(Collections.singletonMap(item.qualifiedName(), item));

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    when(graphDiagram.getDiagramHeaderLines()).thenReturn(new String[] {"Test Diagram"});
    graphDiagram.draw(context);
    ArgumentCaptor<MutableGraph> graphArgumentCaptor = ArgumentCaptor.forClass(MutableGraph.class);
    verify(graphDiagram).writGraphToFile(any(File.class), graphArgumentCaptor.capture());
    MutableGraph generatedGraph = graphArgumentCaptor.getValue();
    Graphviz.useEngine(new GraphvizV8Engine());
    String jsonGraph = Graphviz.fromGraph(generatedGraph).render(Format.JSON).toString();
    String ref = new String(Files.readAllBytes(Paths
        .get("src/test/java/com/javastreets/mulefd/drawings/drawToValidateGraph_Expected.json")));
    JSONAssert.assertEquals(ref, jsonGraph, JSONCompareMode.STRICT);
    Graphviz.releaseEngine();

  }


  @Test
  @DisplayName("Validate generated graph for APIKIT flows when generated as JSON.")
  void drawToValidateGraph_APIKIT() throws Exception {

    List flows = DiagramRendererTestUtil.getFlows(Paths.get("src/test/resources/test-api.xml"));
    File output = new File(".", "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    context.setComponents(flows);

    ComponentItem item = new ComponentItem();
    item.setPrefix("apikit");
    item.setOperation("*");
    item.setSource(false);
    item.setConfigAttributeName("config-ref");
    item.setPathAttributeName("config-ref");
    context.setKnownComponents(Collections.singletonMap(item.qualifiedName(), item));

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    when(graphDiagram.getDiagramHeaderLines()).thenReturn(new String[] {"Test Diagram"});
    graphDiagram.draw(context);
    ArgumentCaptor<MutableGraph> graphArgumentCaptor = ArgumentCaptor.forClass(MutableGraph.class);
    verify(graphDiagram).writGraphToFile(any(File.class), graphArgumentCaptor.capture());
    MutableGraph generatedGraph = graphArgumentCaptor.getValue();
    Graphviz.useEngine(new GraphvizV8Engine());
    String jsonGraph = Graphviz.fromGraph(generatedGraph).render(Format.JSON).toString();
    String ref = new String(Files.readAllBytes(Paths.get(
        "src/test/java/com/javastreets/mulefd/drawings/drawToValidateGraph_APIKIT_Expected.json")));
    JSONAssert.assertEquals(ref, jsonGraph, JSONCompareMode.STRICT);
    Graphviz.releaseEngine();

  }

  @Test
  @DisplayName("Validate generated graph for Single flow when generated as JSON.")
  void drawToValidateGraph_SingleFlow() throws Exception {

    List flows = DiagramRendererTestUtil
        .getFlows(Paths.get("src/test/resources/single-flow-generation-example.xml"));
    File output = new File(".", "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    context.setComponents(flows);
    context.setFlowName("sub-flow-level-1-2");
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    when(graphDiagram.getDiagramHeaderLines()).thenReturn(new String[] {"Test Diagram"});
    graphDiagram.draw(context);
    ArgumentCaptor<MutableGraph> graphArgumentCaptor = ArgumentCaptor.forClass(MutableGraph.class);
    verify(graphDiagram).writGraphToFile(any(File.class), graphArgumentCaptor.capture());
    MutableGraph generatedGraph = graphArgumentCaptor.getValue();
    Graphviz.useEngine(new GraphvizV8Engine());
    String jsonGraph = Graphviz.fromGraph(generatedGraph).render(Format.JSON).toString();
    System.out.println(jsonGraph);
    String ref = new String(
        Files.readAllBytes(Paths.get("src/test/resources/single-flow-generation-example.json")));
    JSONAssert.assertEquals(ref, jsonGraph, JSONCompareMode.STRICT);
    Graphviz.releaseEngine();

  }

  @Test
  void drawWithSinglesGeneration() {
    // Get the Java runtime
    Runtime runtime = Runtime.getRuntime();
    // Run the garbage collector
    runtime.gc();
    File output = new File(tempDir, "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    context.setGenerateSingles(true);
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow");
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");

    // Add reference to same sub-flow, resulting loop
    subflow.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));

    context.setComponents(Arrays.asList(flowContainer, subflow));
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    graphDiagram.draw(context);
    runtime.gc();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory is bytes: " + memory);
    System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));
    assertThat(output).exists();
    verify(graphDiagram, Mockito.times(1)).writeFlowGraph(any(), any(), any());
    verify(graphDiagram, Mockito.times(1)).writeFlowGraph(eq(flowContainer), any(), any());
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
    verify(graphDiagram, Mockito.times(3)).processComponent(compArg.capture(), eq(context),
        anyMap(), anyList());
    assertThat(compArg.getAllValues()).containsExactly(flowContainer2, subflow, subflow);
    logs.assertContains(
        "Detected a possible self loop in sub-flow test-sub-flow. Skipping flow-ref processing.");
  }


  @Test
  @DisplayName("Throw error when given flow name not found")
  void drawASingleFlow_WhenFlowNotFound() {
    // Get the Java runtime
    Runtime runtime = Runtime.getRuntime();
    // Run the garbage collector
    runtime.gc();
    File output = new File(tempDir, "output.png");
    DrawingContext context = new DrawingContext();
    context.setDiagramType(DiagramType.GRAPH);
    context.setOutputFile(output);
    context.setFlowName("wrong-flow-name");
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow-1");

    context.setComponents(Arrays.asList(flowContainer));
    context.setKnownComponents(Collections.emptyMap());

    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    Throwable throwable = catchThrowable(() -> graphDiagram.draw(context));
    assertThat(throwable).isNotNull().hasMessage("Target flow not found - wrong-flow-name");
  }

  @Test
  void supports() {
    GraphDiagram graphDiagram = new GraphDiagram();
    assertThat(graphDiagram.supports(DiagramType.GRAPH)).isTrue();
  }

  @Test
  void name() {
    GraphDiagram graphDiagram = new GraphDiagram();
    assertThat(graphDiagram.name()).isEqualTo("Graph");
  }

  @Test
  void initNewGraph() {
    GraphDiagram graphDiagram = Mockito.spy(new GraphDiagram());
    Mockito.when(graphDiagram.getDiagramHeaderLines()).thenReturn(new String[] {"Test"});
    MutableGraph graph = mutGraph("mule").setDirected(true).linkAttrs()
        .add(VEE.dir(Arrow.DirType.FORWARD)).graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT),
            GraphAttr.splines(GraphAttr.SplineMode.SPLINE), GraphAttr.pad(2.0), GraphAttr.dpi(150),
            Label.htmlLines("Test").locate(Label.Location.TOP));
    MutableGraph returnedGraph = graphDiagram.initNewGraph();
    assertThat(returnedGraph).isEqualTo(graph);
  }

  @Test
  void writGraphToFile() throws Exception {
    GraphDiagram graphDiagram = new GraphDiagram();
    MutableGraph graph = graphDiagram.initNewGraph();
    boolean generated = graphDiagram.writGraphToFile(new File(tempDir, "test.png"), graph);
    assertThat(generated).isTrue();
  }

  @Test
  void writeFlowGraphWithFlow() {
    GraphDiagram graphDiagram = new GraphDiagram();
    MutableGraph graph = graphDiagram.initNewGraph();
    FlowContainer flowContainer = new FlowContainer("flow", "test-flow");
    flowContainer.addComponent(new MuleComponent("flow-ref", "test-sub-flow"));
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");
    Path outputFilePath = Paths.get(tempDir.getAbsolutePath(), "dummy");
    boolean written = graphDiagram.writeFlowGraph(flowContainer, outputFilePath, graph);
    assertThat(written).isTrue();
    assertThat(graph.name()).isEqualTo(Label.of(flowContainer.qualifiedName()));
    assertThat(Files.exists(Paths.get(outputFilePath.toString(), flowContainer.getName() + ".png")))
        .isTrue();
  }

  @Test
  void writeFlowGraphWithSubFlow() {
    GraphDiagram graphDiagram = new GraphDiagram();
    MutableGraph graph = graphDiagram.initNewGraph();
    FlowContainer subflow = new FlowContainer("sub-flow", "test-sub-flow");
    Path outputFilePath = Paths.get(tempDir.getAbsolutePath(), "dummy");
    boolean written = graphDiagram.writeFlowGraph(subflow, outputFilePath, graph);
    assertThat(written).isFalse();
  }
}
