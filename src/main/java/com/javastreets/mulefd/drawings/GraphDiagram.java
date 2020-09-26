package com.javastreets.mulefd.drawings;

import static com.javastreets.mulefd.util.FileUtil.sanitizeFilename;
import static guru.nidi.graphviz.attribute.Arrow.*;
import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javastreets.mulefd.app.DrawingException;
import com.javastreets.mulefd.model.Component;
import com.javastreets.mulefd.model.FlowContainer;
import com.javastreets.mulefd.model.MuleComponent;
import com.javastreets.mulefd.util.DateUtil;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

public class GraphDiagram implements Diagram {

  Logger log = LoggerFactory.getLogger(GraphDiagram.class);

  @Override
  public boolean draw(DrawingContext drawingContext) {
    MutableGraph rootGraph = initNewGraph();
    File targetDirectory = drawingContext.getOutputFile().getParentFile();

    Map<String, Component> flowRefs = new HashMap<>();
    List<String> mappedFlowKinds = new ArrayList<>();
    List<Component> flows = drawingContext.getComponents();
    Path singleFlowDirPath = Paths.get(targetDirectory.getAbsolutePath(), "single-flow-diagrams",
        DateUtil.now("ddMMyyyy-HHmmss"));
    if (drawingContext.getFlowName() != null) {
      Component component = flows.stream()
          .filter(component1 -> component1.getName().equalsIgnoreCase(drawingContext.getFlowName()))
          .findFirst().orElseThrow(() -> new DrawingException(
              "Target flow not found - " + drawingContext.getFlowName()));
      MutableNode flowNode = processComponent(component, drawingContext, flowRefs, mappedFlowKinds);
      flowNode.addTo(rootGraph);
    }

    for (Component component : flows) {
      if (drawingContext.getFlowName() == null
          || mappedFlowKinds.contains(component.qualifiedName())) {

        MutableNode flowNode =
            processComponent(component, drawingContext, flowRefs, mappedFlowKinds);

        if (drawingContext.isGenerateSingles() && component.isaFlow()) {
          MutableGraph flowGraph = initNewGraph();
          flowNode.addTo(flowGraph);
          for (Component component2 : flows) {
            if (mappedFlowKinds.contains(component2.qualifiedName())) {
              MutableNode flowNode3 =
                  processComponent(component2, drawingContext, flowRefs, mappedFlowKinds);
              flowNode3.addTo(flowGraph);
            }
          }
          writeFlowGraph(component, singleFlowDirPath, flowGraph);
        }
        flowNode.addTo(rootGraph);
      }
    }
    if (drawingContext.getFlowName() == null) {
      checkUnusedNodes(rootGraph);
    }
    return writGraphToFile(drawingContext.getOutputFile(), rootGraph);
  }

  boolean writeFlowGraph(Component flowComponent, Path targetDirectory, MutableGraph flowGraph) {
    if (!flowComponent.isaFlow())
      return false;
    String flowName = flowComponent.getName();
    Path targetPath =
        Paths.get(targetDirectory.toString(), sanitizeFilename(flowName.concat(".png")));
    log.info("Writing individual flow graph for {} at {}", flowName, targetPath);
    try {
      flowGraph.setName(flowComponent.qualifiedName());
      Files.createDirectories(targetPath);
      writGraphToFile(targetPath.toFile(), flowGraph);
    } catch (IOException e) {
      log.error("Error while creating parent directory for {}", targetPath, e);
      log.error("Skipping individual graph generation for flow {}", flowName);
      return false;
    }
    return true;
  }

  boolean writGraphToFile(File outputFilename, MutableGraph graph) {
    try {
      log.debug("Writing graph at path {}", outputFilename);
      Graphviz.useEngine(new GraphvizV8Engine());
      boolean generated =
          Graphviz.fromGraph(graph).render(Format.PNG).toFile(outputFilename).exists();
      Graphviz.releaseEngine();
      return generated;
    } catch (IOException e) {
      log.error("Error while writing graph at {}", outputFilename, e);
      return false;
    }
  }

  MutableGraph initNewGraph() {
    return mutGraph("mule").setDirected(true).linkAttrs().add(VEE.dir(DirType.FORWARD)).graphAttrs()
        .add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT), GraphAttr.splines(GraphAttr.SplineMode.SPLINE),
            GraphAttr.pad(2.0), GraphAttr.dpi(150),
            Label.htmlLines(getDiagramHeaderLines()).locate(Label.Location.TOP));
  }

  private void checkUnusedNodes(MutableGraph graph) {
    graph.nodes().stream()
        .filter(node -> node.links().isEmpty() && graph.edges().stream().noneMatch(
            edge -> edge.to().name().equals(node.name()) || edge.from().name().equals(node.name())))
        .forEach(node -> node.add(Color.RED, Style.FILLED, Color.GRAY));
  }

  MutableNode processComponent(Component component, DrawingContext drawingContext,
      Map<String, Component> flowRefs, List<String> mappedFlowKinds) {
    log.debug("Processing flow - {}", component.qualifiedName());
    FlowContainer flow = (FlowContainer) component;
    Consumer<MutableNode> asFlow = flowNode -> flowNode.add(Shape.RECTANGLE).add(Color.BLUE);
    MutableNode flowNode = mutNode(flow.qualifiedName()).add(Label.markdown(getNodeLabel(flow)));
    if (flow.isaSubFlow()) {
      flowNode.add(Color.BLACK).add(Shape.ELLIPSE);
    } else {
      asFlow.accept(flowNode);
    }
    MutableNode sourceNode = null;
    boolean hasSource = false;
    for (int componentIdx = 1; componentIdx <= flow.getComponents().size(); componentIdx++) {
      MuleComponent muleComponent = flow.getComponents().get(componentIdx - 1);
      // Link style should be done with .linkTo()
      String name = muleComponent.qualifiedName();
      if (muleComponent.isaFlowRef()) {
        Component refComponent = flowRefs.computeIfAbsent(muleComponent.getName(),
            k -> targetFlowByName(muleComponent.getName(), drawingContext.getComponents()));
        if (refComponent != null) {
          if (refComponent.equals(flow)) {
            log.warn("Detected a possible self loop in {} {}. Skipping flow-ref processing.",
                refComponent.getType(), refComponent.getName());
            flowNode.addLink(flowNode);
            mappedFlowKinds.add(name);
            continue;
          } else {
            name = refComponent.qualifiedName();
            if (!mappedFlowKinds.contains(name)) {
              processComponent(refComponent, drawingContext, flowRefs, mappedFlowKinds);
            }
          }
        }
      }
      if (muleComponent.isSource()) {
        hasSource = true;
        sourceNode =
            mutNode(name).add(Shape.HEXAGON, Color.DARKORANGE).add("sourceNode", Boolean.TRUE).add(
                Label.htmlLines("<b>" + muleComponent.getType() + "</b>", muleComponent.getName()));
      } else if (muleComponent.getType().equals("apikit")) {
        // APIKit auto generated flows follow a naming pattern
        // "{httpMethod}:\{resource-name}:{apikitConfigName}"
        // 1. Create a new apikit node for this component
        // 2. Find all flows with name ending with ":{apikiConfigName}"
        // 3. Link those flows with apiKit flow.
        log.debug("Processing apikit component - {}", component.qualifiedName());
        MutableNode apiKitNode =
            mutNode(muleComponent.getType().concat(muleComponent.getConfigRef().getValue()))
                .add(Label.htmlLines("<b>" + muleComponent.getType() + "</b>",
                    muleComponent.getConfigRef().getValue()))
                .add(Shape.DOUBLE_CIRCLE, Color.CYAN, Style.FILLED);
        for (Component component1 : searchFlowBySuffix(
            ":" + muleComponent.getConfigRef().getValue(), drawingContext.getComponents())) {
          MutableNode node =
              mutNode(component1.qualifiedName()).add(Label.markdown(getNodeLabel(component1)));
          asFlow.accept(node);
          apiKitNode.addLink(to(node).with(Style.SOLID));
        }
        flowNode
            .addLink(to(apiKitNode).with(Style.SOLID, Label.of("(" + (componentIdx - 1) + ")")));
      } else {
        addSubNodes(flowNode, hasSource ? componentIdx - 1 : componentIdx, muleComponent, name);
      }

      mappedFlowKinds.add(name);
    }
    if (sourceNode != null) {
      flowNode = sourceNode.add(Style.FILLED, Color.CYAN).addLink(to(flowNode).with(Style.BOLD));
    }
    return flowNode;
  }

  private String getNodeLabel(Component component) {
    return String.format("**%s**: %s", component.getType(), component.getName());
  }

  private void addSubNodes(MutableNode flowNode, int callSequence, MuleComponent muleComponent,
      String name) {
    if (muleComponent.isAsync()) {
      flowNode.addLink(to(mutNode(name)).with(Style.DASHED.and(Style.BOLD),
          Label.of("(" + callSequence + ") Async"), Color.BROWN));
    } else {
      flowNode.addLink(to(mutNode(name)).with(Style.SOLID, Label.of("(" + callSequence + ")")));
    }
  }

  @Override
  public boolean supports(DiagramType diagramType) {
    return DiagramType.GRAPH.equals(diagramType);
  }

  @Override
  public String name() {
    return "Graph";
  }
}
