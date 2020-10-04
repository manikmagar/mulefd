package com.javastreets.mulefd;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.javastreets.mulefd.app.CommandModel;
import com.javastreets.mulefd.drawings.DiagramType;
import com.javastreets.mulefd.drawings.DrawingContext;
import com.javastreets.mulefd.model.FlowContainer;

public class DiagramRendererTestUtil {


  public static CommandModel getCommandModel(Path sourcePath) {
    CommandModel model = new CommandModel();
    model.setDiagramType(DiagramType.GRAPH);
    model.setOutputFilename("test-output-file");
    model.setTargetPath(Paths.get("dummy-result-path"));
    model.setSourcePath(sourcePath);

    return model;
  }


  public static List<FlowContainer> getFlows(Path sourcePath) throws IOException {
    CommandModel commandModel = getCommandModel(sourcePath);
    DiagramRenderer diagramRenderer = new DiagramRenderer(commandModel);
    return diagramRenderer.findFlows(diagramRenderer.drawingContext(commandModel));
  }

  public static DrawingContext getDrawingContext(Path sourcePath) throws IOException {
    CommandModel commandModel = getCommandModel(sourcePath);
    DiagramRenderer diagramRenderer = new DiagramRenderer(commandModel);
    DrawingContext context = diagramRenderer.drawingContext(commandModel);
    List<FlowContainer> flows = diagramRenderer.findFlows(context);
    context.setComponents(Collections.unmodifiableList(flows));
    return context;
  }

}
