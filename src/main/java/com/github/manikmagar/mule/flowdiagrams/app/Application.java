package com.github.manikmagar.mule.flowdiagrams.app;

import com.github.manikmagar.mule.flowdiagrams.DiagramRenderer;
import com.github.manikmagar.mule.flowdiagrams.drawings.DiagramType;
import picocli.CommandLine.Command;

import java.nio.file.Paths;

@Command(name = "mule-flow-diagrams", mixinStandardHelpOptions = true,
    version = "Mule Flow Diagrams 0.0.1",
    description = "Create Flow Diagrams from mule configuration files.")
public class Application {

  private String sourcePath;

  public static void main(String[] args) {
    CommandModel cm = new CommandModel();
    cm.setSourcePath(Paths.get("./src/test/mule"));
    cm.setResultPath(Paths.get("example"));
    cm.setDiagramType(DiagramType.GRAPH);
    cm.setOutputFilename("mule" + System.currentTimeMillis() + ".png");
    new DiagramRenderer(cm).buildModel();
  }
}
