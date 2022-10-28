package com.javastreets.mulefd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javastreets.mulefd.cli.CommandModel;
import com.javastreets.mulefd.drawings.Diagram;
import com.javastreets.mulefd.drawings.DrawingContext;
import com.javastreets.mulefd.drawings.DrawingException;
import com.javastreets.mulefd.model.ComponentItem;
import com.javastreets.mulefd.model.FlowContainer;

import javax.xml.xpath.XPathExpressionException;

public class DiagramRenderer {
  public static final int MULE_VERSION_4 = 4;
  public static final int MULE_VERSION_3 = 3;
  public static final String DEFAULT_MULE_COMPONENTS_CSV = "default-mule-components.csv";
  public static final String MULE_CUSTOM_COMPONENTS_CSV = "mulefd-components.csv";
  Logger log = LoggerFactory.getLogger(DiagramRenderer.class);

  private CommandModel commandModel;

  public DiagramRenderer(CommandModel commandModel) {
    this.commandModel = commandModel;
  }

  Map<String, ComponentItem> prepareKnownComponents() {
    Map<String, ComponentItem> items = new HashMap<>();
    try {
      loadComponentsFile(items, new InputStreamReader(Objects.requireNonNull(Thread.currentThread()
          .getContextClassLoader().getResourceAsStream(DEFAULT_MULE_COMPONENTS_CSV))));
    } catch (IOException e) {
      log.error("Error while loading default mule components file.", e);
    }
    Path mulefdComponents = this.commandModel.getSourcePath().toFile().isDirectory()
        ? this.commandModel.getSourcePath().resolve(MULE_CUSTOM_COMPONENTS_CSV)
        : this.commandModel.getSourcePath().resolveSibling(MULE_CUSTOM_COMPONENTS_CSV);
    if (Files.exists(mulefdComponents)) {
      try {
        log.info("Found {}. This will be loaded to register any custom modules/connectors.",
            mulefdComponents);
        loadComponentsFile(items, new InputStreamReader(Files.newInputStream(mulefdComponents)));
      } catch (IOException e) {
        throw new DrawingException("Unable to load file - " + MULE_CUSTOM_COMPONENTS_CSV, e);
      }
    }
    return items;
  }

  private void loadComponentsFile(Map<String, ComponentItem> items, InputStreamReader reader)
      throws IOException {
    try (BufferedReader br = new BufferedReader(reader)) {
      for (String line; (line = br.readLine()) != null;) {
        if (!line.startsWith("prefix")) {
          log.debug("Reading component line - {}", line);
          String[] part = line.split(",");
          if (part.length != 6) {
            log.error(
                "Found an invalid configuration line in mule components file. Column count must be 5. Line - {}",
                line);
            throw new DrawingException("Invalid mule components configuration file.");
          }
          ComponentItem item = new ComponentItem();
          item.setPrefix(part[0]);
          item.setOperation(part[1]);
          item.setSource(Boolean.valueOf(part[2]));
          if (item.getOperation().equals("*") && item.isSource()) {
            log.error(
                "Wildcard operation entry as a source is not allowed. Please create a separate entry for source if needed. Line - {}",
                line);
            throw new DrawingException("Invalid mule components configuration file.");
          }
          item.setPathAttributeName(part[3]);
          item.setConfigAttributeName(part[4]);
          item.setAsync(Boolean.valueOf(part[5]));
          items.putIfAbsent(item.qualifiedName(), item);
        }
      }
      // line is not visible here.
    }
  }

  public Boolean render() {
    try {
      DrawingContext context = drawingContext(commandModel);
      List<FlowContainer> flows = findFlows(context);
      context.setComponents(Collections.unmodifiableList(flows));
      if (commandModel.getFlowName() != null && flows.stream().noneMatch(
          flowContainer -> flowContainer.getName().equalsIgnoreCase(commandModel.getFlowName()))) {
        throw new DrawingException("Flow not found - " + commandModel.getFlowName());
      }
      return diagram(context);
    } catch (IOException e) {
      log.error("Error while parsing xml file", e);
      return false;
    } catch (XPathExpressionException e) {
      log.error("Error while interpreting path", e);
      return false;
    }
  }

  boolean existInSource(String path) {
    return Files.exists(Paths.get(commandModel.getSourcePath().toString(), path));
  }

  List<FlowContainer> findFlows(DrawingContext context) throws IOException, XPathExpressionException {
    Path newSourcePath = getMuleSourcePath();
    List<FlowContainer> flows = new ArrayList<>();
    try (Stream<Path> paths = Files.walk(newSourcePath)) {
      List<Path> xmls = paths
          .filter(
              path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".xml"))
          .collect(Collectors.toList());
      for (Path path : xmls) {
        flows(flows, context.getKnownComponents(), path);
      }
    }
    return flows;
  }

  Path getMuleSourcePath() {
    Path newSourcePath = commandModel.getSourcePath();
    commandModel.setMuleVersion(MULE_VERSION_4);
    if (Files.isDirectory(commandModel.getSourcePath())) {
      log.debug("Source is a directory {}", commandModel.getSourcePath());
      if (existInSource("src/main/mule/") && existInSource("mule-artifact.json")) {
        log.info(
            "Found standard Mule 4 source structure 'src/main/mule'. Source is a Mule-4 project.");
        newSourcePath = Paths.get(commandModel.getSourcePath().toString(), "src/main/mule");
        commandModel.setMuleVersion(MULE_VERSION_4);
      } else if (existInSource("src/main/app/") && existInSource("mule-project.xml")) {
        log.info(
            "Found standard Mule 3 source structure 'src/main/app'. Source is a Mule-3 project.");
        newSourcePath = Paths.get(commandModel.getSourcePath().toString(), "src/main/app");
        commandModel.setMuleVersion(MULE_VERSION_3);
      } else {
        log.warn(
            "No known standard Mule (3/4) directory structure found (src/main/mule or src/main/app).");
      }
      log.info(
          "Source directory '{}' will be scanned recursively to find Mule {} configuration files.",
          newSourcePath, commandModel.getMuleVersion());
    } else {
      log.info("Reading source file {}", newSourcePath);
    }
    return newSourcePath;
  }

  void flows(List<FlowContainer> flows, Map<String, ComponentItem> knownComponents, Path path) throws XPathExpressionException {
    log.debug("Reading file {}", path);
    MuleXmlParser muleXmlParser = new MuleXmlParser(path.toAbsolutePath().toString());
    muleXmlParser.parse();
    if (muleXmlParser.isMuleFile()) {
      flows.addAll(muleXmlParser.getMuleFlows(knownComponents));
    } else {
      log.debug("Not a mule configuration file: {}", path);
    }
  }

  Boolean diagram(DrawingContext context) {
    if (context.getComponents().isEmpty()) {
      log.warn("No mule flows found for creating diagram.");
      return false;
    }
    ServiceLoader<Diagram> diagramServices = ServiceLoader.load(Diagram.class);
    Iterator<Diagram> its = diagramServices.iterator();
    boolean drawn = false;
    while (its.hasNext()) {
      Diagram diagram = its.next();
      log.debug("Analyzing diagram provider {}", diagram.getClass());
      if (diagram.supports(commandModel.getDiagramType())) {
        log.debug("Found a supporting provider {} for drawing {}", diagram.getClass(),
            commandModel.getDiagramType());
        log.info("Initiating drawing {} at {}", diagram.name(), commandModel.getTargetPath());
        if (context.getFlowName() != null) {
          log.info("Generating diagram for dependencies of single flow only - {}",
              context.getFlowName());
        }
        drawn = diagram.draw(context);
        log.info("Generated {} at {}", diagram.name(), context.getOutputFile().getAbsolutePath());
        break;
      }
    }
    return drawn;
  }

public DrawingContext drawingContext(CommandModel model) {
    DrawingContext context = new DrawingContext();
    context.setDiagramType(model.getDiagramType());
    context.setOutputFile(new File(model.getTargetPath().toFile(), model.getOutputFilename()));
    context.setFlowName(model.getFlowName());
    context.setGenerateSingles(model.isGenerateSingles());
    context.setKnownComponents(prepareKnownComponents());
    return context;
  }
}
