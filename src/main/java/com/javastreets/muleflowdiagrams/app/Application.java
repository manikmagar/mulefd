package com.javastreets.muleflowdiagrams.app;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.javastreets.muleflowdiagrams.DiagramRenderer;
import com.javastreets.muleflowdiagrams.drawings.DiagramType;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "muleflowdiagrams", mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    footer = "\nCopyright: 2020 Manik Magar, License: MIT\nWebsite: https://github.com/manikmagar/mule-flow-diagrams",
    description = "Create Flow diagrams from mule configuration files.", showDefaultValues = true)
public class Application implements Callable<Boolean> {

  @CommandLine.Parameters(index = "0",
      description = "Source directory path containing mule configuration files")
  private Path sourcePath;

  @CommandLine.Option(names = {"-t", "--target"},
      description = "Output directory path to generate diagram")
  private Path targetPath;

  @CommandLine.Option(names = {"-d", "--diagram"}, defaultValue = "GRAPH",
      description = "Type of diagram to generate. Valid values: ${COMPLETION-CANDIDATES}")
  private DiagramType diagramType;

  @CommandLine.Option(names = {"-o", "--out"}, defaultValue = "mule-diagram",
      description = "Name of the output file")
  private String outputFilename;


  public static void main(String[] args) {
    int exitCode = new CommandLine(new Application()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Boolean call() throws Exception {
    CommandModel cm = new CommandModel();
    cm.setSourcePath(sourcePath);
    cm.setTargetPath(targetPath);
    cm.setDiagramType(diagramType);
    cm.setOutputFilename(outputFilename + ".png");
    return new DiagramRenderer(cm).render();
  }
}
