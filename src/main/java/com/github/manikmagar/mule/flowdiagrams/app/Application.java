package com.github.manikmagar.mule.flowdiagrams.app;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.github.manikmagar.mule.flowdiagrams.DiagramRenderer;
import com.github.manikmagar.mule.flowdiagrams.drawings.DiagramType;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "mule-flow-diagrams", mixinStandardHelpOptions = true,
    version = "Mule Flow Diagrams 0.1.0",
    description = "Create Flow Diagrams from mule configuration files.")
public class Application implements Callable<Boolean> {

  @CommandLine.Parameters(paramLabel = "Source Path", index = "0",
      description = "Directory path where mule configuration files exist")
  private Path sourcePath;

  @CommandLine.Option(names = {"-o", "--out"}, paramLabel = "Output path",
      description = "Output path where resulted diagram file will be generated.")
  private Path resultPath;

  @CommandLine.Option(names = {"-d", "--diagram"}, paramLabel = "Diagram Type",
      defaultValue = "GRAPH")
  private DiagramType diagramType;

  @CommandLine.Option(names = {"-outName"}, paramLabel = "Output filename",
      defaultValue = "mule-diagram")
  private String outputFilename;


  public static void main(String[] args) {
    int exitCode = new CommandLine(new Application()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Boolean call() throws Exception {
    CommandModel cm = new CommandModel();
    cm.setSourcePath(sourcePath);
    cm.setResultPath(resultPath);
    cm.setDiagramType(diagramType);
    cm.setOutputFilename(outputFilename + ".png");
    return new DiagramRenderer(cm).render();
  }
}
