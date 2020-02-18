package com.github.manikmagar.mule.flowdiagrams;

import com.github.manikmagar.mule.flowdiagrams.app.CommandModel;
import com.github.manikmagar.mule.flowdiagrams.drawings.Diagram;
import com.github.manikmagar.mule.flowdiagrams.drawings.DrawingContext;
import com.github.manikmagar.mule.flowdiagrams.model.ComponentItem;
import com.github.manikmagar.mule.flowdiagrams.model.MuleFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DiagramRenderer {
  Logger log = LoggerFactory.getLogger(DiagramRenderer.class);

  private CommandModel commandModel;

  public DiagramRenderer(CommandModel commandModel) {
    this.commandModel = commandModel;
  }

  private List<ComponentItem> prepareKnownComponents() {
    List<String> lines = null;
    try {
      lines = Files.readAllLines(Paths.get(Thread.currentThread().getContextClassLoader()
          .getResource("mule-components.csv").toURI()));
      List<ComponentItem> items = new ArrayList<>();
      for (String line : lines) {
        if (!line.startsWith("prefix")) {
          String[] part = line.split(",");
          ComponentItem item = new ComponentItem();
          item.setPrefix(part[0]);
          item.setOperation(part[1]);
          item.setSource(Boolean.valueOf(part[2]));
          item.setPath(part[3]);
          items.add(item);
        }
      }
      return items;
    } catch (IOException | URISyntaxException e) {
      log.error("mule-components file not found", e);
    }
    return Collections.emptyList();
  }

  public void buildModel() {
    try {
      List<Path> xmls = Files.walk(commandModel.getSourcePath())
          .filter(path -> path.toFile().isFile()).collect(Collectors.toList());
      List<MuleFlow> flows = new ArrayList<>();

      for (Path path : xmls) {
        log.debug("Reading file {}", path);
        MuleXmlParser muleXmlParser = new MuleXmlParser(path.toAbsolutePath().toString());
        muleXmlParser.parse();
        if (muleXmlParser.isMuleFile()) {
          flows.addAll(muleXmlParser.getMuleFlows());
        } else {
          log.debug("Not a mule configuration file: {}", path);
        }
      }


      if (flows.isEmpty()) {
        log.warn("No mule flows found for creating diagram.");
      } else {
        DrawingContext context = toDrawingContext(commandModel);
        context.setComponents(Collections.unmodifiableList(flows));
        context.setKnownComponents(prepareKnownComponents());
        ServiceLoader<Diagram> diagramServices = ServiceLoader.load(Diagram.class);
        Iterator<Diagram> its = diagramServices.iterator();
        while (its.hasNext()) {
          Diagram diagram = its.next();
          log.debug("Analyzing diagram provider {}", diagram.getClass());
          if (diagram.supports(commandModel.getDiagramType())) {
            log.debug("Found a supporting provider {} for drawing {}", diagram.getClass(),
                commandModel.getDiagramType());
            log.info("Initiating drawing {} at {}", diagram.name(), commandModel.getResultPath());
            diagram.draw(context);
            log.info("Generated {} at {}", diagram.name(),
                context.getOutputFile().getAbsolutePath());
            break;
          }
        }
      }

    } catch (IOException e) {
      log.error("Error while parsing xml file", e);
    }
  }

  public DrawingContext toDrawingContext(CommandModel model) {
    DrawingContext context = new DrawingContext();
    context.setDiagramType(model.getDiagramType());
    context.setOutputFile(new File(model.getResultPath().toFile(), model.getOutputFilename()));
    return context;
  }
}
