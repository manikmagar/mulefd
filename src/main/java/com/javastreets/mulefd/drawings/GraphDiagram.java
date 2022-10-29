package com.javastreets.mulefd.drawings;

import static com.javastreets.mulefd.util.FileUtil.sanitizeFilename;
import static guru.nidi.graphviz.attribute.Arrow.DirType;
import static guru.nidi.graphviz.attribute.Arrow.VEE;
import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javastreets.mulefd.cli.Configuration;
import com.javastreets.mulefd.drawings.engine.GraphvizEngineHelper;
import com.javastreets.mulefd.model.Component;
import com.javastreets.mulefd.model.FlowContainer;
import com.javastreets.mulefd.model.KnownMuleComponent;
import com.javastreets.mulefd.model.MuleComponent;
import com.javastreets.mulefd.util.DateUtil;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

public class GraphDiagram implements Diagram {

  Logger log = LoggerFactory.getLogger(GraphDiagram.class);

  @Override
  public boolean draw(DrawingContext drawingContext) {
    MutableGraph rootGraph = initNewGraphWithLegend(true);
    MutableGraph appGraph = addApplicationGraph(rootGraph);
    File targetDirectory = drawingContext.getOutputFile().getParentFile();
    Map<String, Component> flowRefs = new HashMap<>();
    List<String> mappedFlowKinds = new ArrayList<>();
    List<Component> flows = drawingContext.getComponents();
    Path singleFlowDirPath = Paths.get(targetDirectory.getAbsolutePath(), "single-flow-diagrams",
        DateUtil.now("ddMMyyyy-HHmmss"));

    // Some non-flow type components have a valid reference to other flows.
    // Eg. APIKit router can call another flow. It is possible for that flow to not reference any
    // other flow.
    // In such cases, APIKit router should be treated as root Graph Node to find usable links.
    // Accumulate all such components to be added as a root node.
    // TODO: Find alternate way to traverse node links
    List<MutableNode> additionalRootNodes = new ArrayList<>();

    if (drawingContext.getFlowName() != null) {
      Component component = flows.stream()
          .filter(component1 -> component1.getName().equalsIgnoreCase(drawingContext.getFlowName()))
          .findFirst().orElseThrow(() -> new DrawingException(
              "Target flow not found - " + drawingContext.getFlowName()));
      MutableNode flowNode = processComponent(component, drawingContext, flowRefs, mappedFlowKinds,
          additionalRootNodes);
      flowNode.addTo(appGraph);
    }

    for (Component component : flows) {
      if (drawingContext.getFlowName() == null
          || mappedFlowKinds.contains(component.qualifiedName())) {

        MutableNode flowNode = processComponent(component, drawingContext, flowRefs,
            mappedFlowKinds, additionalRootNodes);

        if (drawingContext.isGenerateSingles() && component.isaFlow()) {
          MutableGraph flowRootGraph = initNewGraph(getDiagramHeaderLines());
          flowNode.addTo(flowRootGraph);
          for (Component component2 : flows) {
            if (mappedFlowKinds.contains(component2.qualifiedName())) {
              MutableNode flowNode3 = processComponent(component2, drawingContext, flowRefs,
                  mappedFlowKinds, additionalRootNodes);
              flowNode3.addTo(flowRootGraph);
            }
          }
          writeFlowGraph(component, singleFlowDirPath, flowRootGraph);
        }
        flowNode.addTo(appGraph);
      }
    }
    // any flows that are referenced by just the non-flow like nodes e.g APIKIT
    // this adds those component nodes to the root graph so links are visible via node lookup
    additionalRootNodes.forEach(node -> node.addTo(appGraph));
    if (drawingContext.getFlowName() == null) {
      checkUnusedNodes(appGraph);
    }
    return writGraphToFile(drawingContext.getOutputFile(), rootGraph);
  }

  MutableGraph addApplicationGraph(MutableGraph rootGraph) {
    MutableGraph appGraph = initNewGraph("Application graph");
    appGraph.setCluster(true).graphAttrs().add(Style.INVIS);
    appGraph.addTo(rootGraph);
    return appGraph;
  }

  MutableNode sizedNode(String label, double width) {
    return mutNode(label).add(Size.mode(Size.Mode.FIXED).size(width, 0.25));
  }

  void addLegends(MutableGraph rootGraph) {
    log.debug("Adding legend to graph - {}", rootGraph.name());
    graph("legend").directed().cluster().graphAttr().with(Label.html("<b>Legend</b>"), Style.DASHED)
        .with(
            asFlow(sizedNode("flow", 1))
                .addLink(to(asSubFlow(sizedNode("sub-flow", 1))).with(Style.INVIS)),
            possibleKnownComponent(new KnownMuleComponent("db", "insert"), "connector:operation"),
            asSubFlow(sizedNode("sub-flow", 1))
                .addLink(to(asUnusedFlow(sizedNode("Unused sub/-flow", 2))).with(Style.INVIS)),
            sizedNode("Flow A", 1).addLink(callSequenceLink(1, sizedNode("sub-flow-1", 1.25))
                .with(Label.lines("Call Sequence").tail(-5, 8))),
            sizedNode("Flow C", 1).addLink(asAsyncLink(1, sizedNode("sub-flow-C1", 1.25))
                .with(Label.lines("Asynchronous call").tail(-5, 8))),
            asSourceNode(sizedNode("flow source", 1.5))
                .addLink(to(asFlow(sizedNode("flow self-call", 1.25))).with(Style.INVIS)),
            asFlow(sizedNode("flow self-call", 2)).addLink(asFlow(sizedNode("flow self-call", 2)))
                .addLink(to(asSubFlow(sizedNode("sub-flow self-call", 2))
                    .addLink(asSubFlow(sizedNode("sub-flow self-call", 2)))).with(Style.INVIS)))
        .addTo(rootGraph);
    // legend-space is added to create gap between application graph and legend.
    // this is a hidden cluster
    graph("legend-space").cluster().graphAttr().with(Label.of(""), Style.INVIS)
        .with(node("").with(Shape.NONE, Size.std().size(2, 1))).addTo(rootGraph);
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
      return GraphvizEngineHelper.generate(graph, Format.PNG, outputFilename);
    } catch (IOException e) {
      log.error("Error while writing graph at {}", outputFilename, e);
      return false;
    }
  }

  MutableGraph initNewGraphWithLegend(boolean legend) {
    MutableGraph rootGraph = initNewGraph(getDiagramHeaderLines());
    if (legend)
      addLegends(rootGraph);
    return rootGraph;
  }

  MutableGraph initNewGraph(String label) {
    return initNewGraph(new String[] {label});
  }

  MutableGraph initNewGraph(String[] label) {
    MutableGraph muleGraph = mutGraph("mule").setDirected(true).linkAttrs()
        .add(VEE.dir(DirType.FORWARD)).graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT),
            GraphAttr.splines(GraphAttr.SplineMode.SPLINE), GraphAttr.pad(1, 0.5),
            GraphAttr.dpi(150), Label.htmlLines(label).locate(Label.Location.TOP));

    String fontName = Configuration.getGlobalConfig().getValue("diagram.font.name", "");
    if (!fontName.trim().isEmpty()) {
      muleGraph = muleGraph.linkAttrs().add(Font.name(fontName)).nodeAttrs()
          .add(Font.name(fontName)).graphAttrs().add(Font.name(fontName));
    }
    return muleGraph;
  }

  private void checkUnusedNodes(MutableGraph graph) {
    graph.nodes().stream()
        .filter(node -> node.links().isEmpty() && graph.edges().stream().noneMatch(
            edge -> edge.to().name().equals(node.name()) || edge.from().name().equals(node.name())))
        .forEach(this::asUnusedFlow);
  }

  MutableNode asUnusedFlow(MutableNode node) {
    return node.add(Color.RED, Style.FILLED, Color.GRAY);
  }

  MutableNode asFlow(MutableNode node) {
    return node.add(Shape.RECTANGLE).add(Color.BLUE);
  }

  MutableNode asSubFlow(MutableNode node) {
    return node.add(Color.BLACK).add(Shape.ELLIPSE);
  }

  MutableNode asApikitNode(String name) {
    return mutNode(name).add(Shape.DOUBLE_CIRCLE, Color.CYAN, Style.FILLED);
  }

  MutableNode asSourceNode(MutableNode node) {
    return node.add(Shape.HEXAGON, Style.FILLED, Color.CYAN).add("sourceNode", Boolean.TRUE);
  }

  MutableNode processComponent(Component component, DrawingContext drawingContext,
      Map<String, Component> flowRefs, List<String> mappedFlowKinds,
      List<MutableNode> additionalRootNodes) {
    log.debug("Processing flow - {}", component.qualifiedName());
    FlowContainer flow = (FlowContainer) component;
    MutableNode flowNode = mutNode(flow.qualifiedName()).add(Label.markdown(getNodeLabel(flow)));
    if (flow.isaSubFlow()) {
      asSubFlow(flowNode);
    } else {
      asFlow(flowNode);
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
              processComponent(refComponent, drawingContext, flowRefs, mappedFlowKinds,
                  additionalRootNodes);
            }
          }
        }
      }
      if (muleComponent.isSource()) {
        hasSource = true;
        String pathValue =
            (muleComponent.getPath() != null) ? muleComponent.getPath().getValue() : "";
        sourceNode = asSourceNode(mutNode(name)).add(Label.htmlLines(
            "<b>" + muleComponent.getType() + ": " + muleComponent.getName() + "</b>", pathValue));
      } else if (muleComponent.getType().equals("apikit")) {
        // APIKit auto generated flows follow a naming pattern
        // "{httpMethod}:\{resource-name}:{apikitConfigName}"
        // 1. Create a new apikit node for this component
        // 2. Find all flows with name ending with ":{apikiConfigName}"
        // 3. Link those flows with apiKit flow.
        log.debug("Processing apikit component - {}", component.qualifiedName());
        MutableNode apiKitNode =
            asApikitNode(muleComponent.getType().concat(muleComponent.getConfigRef().getValue()))
                .add(Label.htmlLines("<b>" + muleComponent.getType() + "</b>",
                    muleComponent.getConfigRef().getValue()));
        for (Component component1 : searchFlowBySuffix(
            ":" + muleComponent.getConfigRef().getValue(), drawingContext.getComponents())) {
          MutableNode node =
              mutNode(component1.qualifiedName()).add(Label.markdown(getNodeLabel(component1)));
          asFlow(node);
          apiKitNode.addLink(to(node).with(Style.SOLID));
        }
        additionalRootNodes.add(apiKitNode);
        flowNode.addLink(callSequenceLink(componentIdx - 1, apiKitNode));
      } else {
        addSubNodes(flowNode, hasSource ? componentIdx - 1 : componentIdx, muleComponent, name);
      }

      mappedFlowKinds.add(name);
    }
    if (sourceNode != null) {
      flowNode = sourceNode.addLink(to(flowNode).with(Style.BOLD));
    }
    return flowNode;
  }

  private String getNodeLabel(Component component) {
    return String.format("**%s**: %s", component.getType(), component.getName());
  }

  private void addSubNodes(MutableNode flowNode, int callSequence, MuleComponent muleComponent,
      String name) {
    MutableNode node = possibleKnownComponent(muleComponent, name);
    if (muleComponent.isAsync()) {
      flowNode.addLink(asAsyncLink(callSequence, node));
    } else {
      flowNode.addLink(callSequenceLink(callSequence, node));
    }
  }

  /**
   * Get a default {@link MutableNode} with given name. If {@link MuleComponent} is a
   * {@link KnownMuleComponent} then adapts the visualization accordingly.
   * 
   * @param muleComponent {@link MuleComponent} to create node for
   * @param name {@link String} name of the node
   * @return MutableNode
   */
  private MutableNode possibleKnownComponent(MuleComponent muleComponent, String name) {
    MutableNode node = mutNode(name);
    if (muleComponent instanceof KnownMuleComponent) {
      node = node.add(Shape.COMPONENT);
      if (muleComponent.getPath() != null && muleComponent.getPath().getValue() != null
          && !muleComponent.getPath().getValue().isEmpty()) {
        String label =
            getNodeLabel(muleComponent) + "  \n<i>" + muleComponent.getPath().getValue() + "</i>";
        node = node.add(Label.markdown(label));
      }
    }
    return node;
  }

  Link callSequenceLink(int callSequence, MutableNode node) {
    return to(node).with(Style.SOLID, Label.of("(" + callSequence + ")"));
  }

  Link asAsyncLink(int callSequence, MutableNode node) {
    return to(node).with(Style.DASHED.and(Style.BOLD),
        Label.of("(" + callSequence + ") Async").external(), Color.LIGHTBLUE3);
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
