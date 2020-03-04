package com.github.manikmagar.mule.flowdiagrams.drawings;

import com.github.manikmagar.mule.flowdiagrams.model.Component;
import com.github.manikmagar.mule.flowdiagrams.model.MuleComponent;
import com.github.manikmagar.mule.flowdiagrams.model.FlowContainer;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Arrow.*;
import static guru.nidi.graphviz.model.Factory.*;

public class GraphDiagram implements Diagram {

  Logger log = LoggerFactory.getLogger(GraphDiagram.class);

  @Override
  public boolean draw(DrawingContext drawingContext) {
    MutableGraph graph = mutGraph("mule").setDirected(true).linkAttrs()
        .add(VEE.dir(DirType.FORWARD)).graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT),
            GraphAttr.splines(GraphAttr.SplineMode.SPLINE), GraphAttr.pad(2.0), GraphAttr.dpi(300),
            Label.htmlLines(getDiagramHeaderLines()).locate(Label.Location.TOP));

    Map<String, Component> flowRefs = new HashMap<>();
    List<String> mappedFlowKinds = new ArrayList<>();
    List<Component> flows = drawingContext.getComponents();
    for (Component component : flows) {
      MutableNode flowNode =
          processComponent(component, graph, drawingContext, flowRefs, mappedFlowKinds);
      flowNode.addTo(graph);
    }
    mutGraph().graphAttrs()
        .add(Rank.inSubgraph(Rank.RankType.SAME), GraphAttr.splines(GraphAttr.SplineMode.LINE))
        .add(flows.stream().filter(Component::isaFlow).map(Component::qualifiedName)
            .map(Factory::node).collect(Collectors.toList()))
        .addTo(graph);
    checkUnusedNodes(graph);
    try {
      Graphviz.useEngine(new GraphvizJdkEngine());
      return Graphviz.fromGraph(graph).render(Format.PNG).toFile(drawingContext.getOutputFile())
          .exists();
    } catch (IOException e) {
      log.error("Error while writing graph", e);
      return false;
    }
  }

  private void checkUnusedNodes(MutableGraph graph) {
    graph.nodes().stream()
        .filter(node -> node.links().isEmpty() && graph.edges().stream().noneMatch(
            edge -> edge.to().name().equals(node.name()) || edge.from().name().equals(node.name())))
        .forEach(node -> node.add(Color.RED, Style.FILLED, Color.GRAY));
  }

  private MutableNode processComponent(Component component, MutableGraph graph,
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
