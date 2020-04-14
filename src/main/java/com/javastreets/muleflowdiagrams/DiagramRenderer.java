package com.javastreets.muleflowdiagrams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javastreets.muleflowdiagrams.app.CommandModel;
import com.javastreets.muleflowdiagrams.drawings.Diagram;
import com.javastreets.muleflowdiagrams.drawings.DrawingContext;
import com.javastreets.muleflowdiagrams.model.ComponentItem;
import com.javastreets.muleflowdiagrams.model.FlowContainer;

public class DiagramRenderer {
  Logger log = LoggerFactory.getLogger(DiagramRenderer.class);

  private CommandModel commandModel;

  public DiagramRenderer(CommandModel commandModel) {
    this.commandModel = commandModel;
  }

  Map<String, ComponentItem> prepareKnownComponents() {
    Map<String, ComponentItem> items = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread()
        .getContextClassLoader().getResourceAsStream("mule-components.csv")))) {
      for (String line; (line = br.readLine()) != null;) {
        if (!line.startsWith("prefix")) {
          log.debug("Reading component line - {}", line);
          String[] part = line.split(",");
          if (part.length != 6) {
            log.error(
                "Found an invalid configuration line in mule components file. Column count must be 5. Line - {}",
                line);
            throw new RuntimeException("Invalid mule components configuration file.");
          }
          ComponentItem item = new ComponentItem();
          item.setPrefix(part[0]);
          item.setOperation(part[1]);
          item.setSource(Boolean.valueOf(part[2]));
          item.setPathAttributeName(part[3]);
          item.setConfigAttributeName(part[4]);
          item.setAsync(Boolean.valueOf(part[5]));
          items.putIfAbsent(item.qualifiedName(), item);
        }
      }
      // line is not visible here.
    } catch (IOException e) {
      log.error("mule-components file not found", e);
    }
    return items;
  }

  public Boolean render() {
    try {
      List<Path> xmls = Files.walk(commandModel.getSourcePath())
          .filter(
              path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".xml"))
          .collect(Collectors.toList());
      List<FlowContainer> flows = new ArrayList<>();
      Map<String, ComponentItem> knownComponents = prepareKnownComponents();
      for (Path path : xmls) {
        flows(flows, knownComponents, path);
      }
      return diagram(flows);
    } catch (IOException e) {
      log.error("Error while parsing xml file", e);
      return false;
    }
  }

  void flows(List<FlowContainer> flows, Map<String, ComponentItem> knownComponents, Path path) {
    log.debug("Reading file {}", path);
    MuleXmlParser muleXmlParser = new MuleXmlParser(path.toAbsolutePath().toString());
    muleXmlParser.parse();
    if (muleXmlParser.isMuleFile()) {
      flows.addAll(muleXmlParser.getMuleFlows(knownComponents));
    } else {
      log.debug("Not a mule configuration file: {}", path);
    }
  }

  Boolean diagram(List<FlowContainer> flows) {
    if (flows.isEmpty()) {
      log.warn("No mule flows found for creating diagram.");
      return false;
    }
    DrawingContext context = drawingContext(commandModel);
    context.setComponents(Collections.unmodifiableList(flows));
    context.setKnownComponents(prepareKnownComponents());
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
    File parent = model.getTargetPath() != null ? model.getTargetPath().toFile() : null;
    if (parent == null) {
      parent = model.getSourcePath().toFile();
      if (parent.isFile()) {
        parent = parent.getParentFile();
      }
    }
    context.setOutputFile(new File(parent, model.getOutputFilename()));
    return context;
  }
}
