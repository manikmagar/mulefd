package com.javastreets.muleflowdiagrams.drawings;

import static guru.nidi.graphviz.attribute.Arrow.*;
import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javastreets.muleflowdiagrams.model.Component;
import com.javastreets.muleflowdiagrams.model.FlowContainer;
import com.javastreets.muleflowdiagrams.model.MuleComponent;
import com.javastreets.muleflowdiagrams.util.Validations;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.Factory;
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
    for (Component component : flows) {
      if (drawingContext.getFlowName() == null
          || component.getName().equalsIgnoreCase(drawingContext.getFlowName())) {
        MutableGraph flowGraph = initNewGraph();
        MutableNode flowNode =
            processComponent(component, flowGraph, drawingContext, flowRefs, mappedFlowKinds);

        flowNode.addTo(flowGraph);
        writeFlowGraph(component, targetDirectory, flowGraph);
        flowGraph.addTo(rootGraph);
      }
    }
    if (drawingContext.getFlowName() == null) {
      mutGraph().setName("subgraph-flows").graphAttrs()
          .add(Rank.inSubgraph(Rank.RankType.SAME), GraphAttr.splines(GraphAttr.SplineMode.LINE))
          .add(flows.stream().filter(Component::isaFlow).map(Component::qualifiedName)
              .map(Factory::node).collect(Collectors.toList()))
          .addTo(rootGraph);
      checkUnusedNodes(rootGraph);
    }
    return writGraphToFile(drawingContext.getOutputFile(), rootGraph);
  }

  boolean writeFlowGraph(Component flowComponent, File targetDirectory, MutableGraph flowGraph) {
    Validations.requireTrue(targetDirectory.isDirectory(), "Target directory must be directory");
    if (!flowComponent.isaFlow())
      return false;
    String flowName = flowComponent.getName();
    Path targetPath = Paths.get(targetDirectory.getAbsolutePath(), "single-flow-diagrams",
        flowName.concat(".png"));
    log.info("Writing individual flow graph for {} at {}", flowName, targetPath.toString());
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

  MutableNode processComponent(Component component, MutableGraph graph,
      DrawingContext drawingContext, Map<String, Component> flowRefs,
      List<String> mappedFlowKinds) {
    FlowContainer flow = (FlowContainer) component;
    MutableNode flowNode = mutNode(flow.qualifiedName()).add(Label.markdown(getNodeLabel(flow)));
    if (flow.isaSubFlow()) {
      flowNode.add(Color.BLACK).add(Shape.ELLIPSE);
    } else {
      flowNode.add(Shape.RECTANGLE).add(Color.BLUE);
    }
    MutableNode sourceNode = null;
    boolean hasSource = false;
    for (int j = 1; j <= flow.getComponents().size(); j++) {
      MuleComponent muleComponent = flow.getComponents().get(j - 1);
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
              processComponent(refComponent, graph, drawingContext, flowRefs, mappedFlowKinds);
            }
          }
        }
      }
      if (muleComponent.isSource()) {
        hasSource = true;
        sourceNode =
            mutNode(name).add(Shape.HEXAGON, Color.DARKORANGE).add("sourceNode", Boolean.TRUE).add(
                Label.htmlLines("<b>" + muleComponent.getType() + "</b>", muleComponent.getName()));
      } else {
        addSubNodes(flowNode, hasSource ? j - 1 : j, muleComponent, name);
      }

      mappedFlowKinds.add(name);
    }
    if (sourceNode != null) {
      sourceNode.add(Style.FILLED, Color.CYAN).addLink(to(flowNode).with(Style.BOLD)).addTo(graph);
    } else {
      flowNode.addTo(graph);
    }
    return flowNode;
  }

  private String getNodeLabel(Component component) {
    return String.format("**%s**: %s", component.getType(), component.getName());
  }

  private void addSubNodes(MutableNode flowNode, int j, MuleComponent muleComponent, String name) {
    if (muleComponent.isAsync()) {
      flowNode.addLink(to(mutNode(name)).with(Style.DASHED.and(Style.BOLD),
          Label.of("(" + j + ") Async"), Color.BROWN));
    } else {
      flowNode.addLink(to(mutNode(name)).with(Style.SOLID, Label.of("(" + j + ")")));
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
