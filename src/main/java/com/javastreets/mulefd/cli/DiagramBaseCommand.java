package com.javastreets.mulefd.cli;

import static com.javastreets.mulefd.util.ConsoleLog.info;

import java.nio.file.Files;
import java.nio.file.Path;

import com.javastreets.mulefd.DiagramRenderer;
import com.javastreets.mulefd.drawings.DiagramType;
import com.javastreets.mulefd.util.DateUtil;
import com.javastreets.mulefd.util.FileUtil;

import picocli.CommandLine;

public abstract class DiagramBaseCommand extends BaseCommand {

  @CommandLine.Parameters(index = "0",
      description = "Source directory path containing mule configuration files")
  private Path sourcePath;

  @CommandLine.Option(names = {"-t", "--target"},
      description = "Output directory path to generate diagram")
  private Path targetPath;

  @CommandLine.Option(names = {"-o", "--out"}, defaultValue = "mule-diagram",
      description = "Name of the output file")
  private String outputFilename;

  @CommandLine.Option(names = {"-fl", "--flowname"},
      description = "Target flow name to generate diagram for. All flows/subflows not related to this flow will be excluded from the diagram.")
  private String flowName;

  @CommandLine.Option(names = {"-gs", "--genSingles"}, defaultValue = "false",
      description = "Generate individual diagrams for each flow.")
  private boolean generateSingles;

  protected abstract DiagramType getDiagramType();

  @Override
  protected Integer execute() {
    info("Mule Flow Diagrams - %s, Started at %s", Version.VersionProvider.getMuleFDVersion(),
        DateUtil.now());
    CommandModel cm = getCommandModel();
    Boolean rendered = new DiagramRenderer(cm).render();
    info("Finished at %s", DateUtil.now());
    return EXIT_OK;
  }


  CommandModel getCommandModel() {
    CommandModel cm = new CommandModel();
    cm.setSourcePath(sourcePath);
    cm.setGenerateSingles(generateSingles);
    cm.setDiagramType(getDiagramType());
    Path resolvedTarget = targetPath;
    if (targetPath == null) {
      if (Files.isDirectory(sourcePath))
        resolvedTarget = sourcePath;
      if (Files.isRegularFile(sourcePath))
        resolvedTarget = sourcePath.toAbsolutePath().getParent();
    }
    cm.setTargetPath(resolvedTarget);
    String filename =
        (outputFilename).endsWith(".png") ? this.outputFilename : outputFilename + ".png";
    if (flowName != null && outputFilename.equalsIgnoreCase("mule-diagram")) {
      filename = flowName + ".png";
    }
    cm.setOutputFilename(FileUtil.sanitizeFilename(filename));
    cm.setFlowName(flowName);
    return cm;
  }
}
