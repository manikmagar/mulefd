package com.javastreets.mulefd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.javastreets.mulefd.cli.CommandModel;
import com.javastreets.mulefd.drawings.DiagramType;
import com.javastreets.mulefd.drawings.DrawingContext;
import com.javastreets.mulefd.model.FlowContainer;

import javax.xml.xpath.XPathExpressionException;

public class DiagramRendererTestUtil {


  public static CommandModel getCommandModel(Path sourcePath, File tempDir) {
    CommandModel model = new CommandModel();
    model.setDiagramType(DiagramType.GRAPH);
    if (tempDir != null) {
      model.setTargetPath(tempDir.toPath());
    } else {
      model.setTargetPath(Paths.get("dummy-result-path"));
    }
    model.setOutputFilename("test-output-file");

    model.setSourcePath(sourcePath);

    return model;
  }


  public static List<FlowContainer> getFlows(Path sourcePath) throws IOException, XPathExpressionException {
    CommandModel commandModel = getCommandModel(sourcePath, null);
    DiagramRenderer diagramRenderer = new DiagramRenderer(commandModel);
    return diagramRenderer.findFlows(diagramRenderer.drawingContext(commandModel));
  }

  public static DrawingContext getDrawingContext(Path sourcePath, File tempDir) throws IOException, XPathExpressionException {
    CommandModel commandModel = getCommandModel(sourcePath, tempDir);
    DiagramRenderer diagramRenderer = new DiagramRenderer(commandModel);
    DrawingContext context = diagramRenderer.drawingContext(commandModel);
    List<FlowContainer> flows = diagramRenderer.findFlows(context);
    context.setComponents(Collections.unmodifiableList(flows));
    return context;
  }

}
