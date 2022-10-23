package com.javastreets.mulefd.cli;


import com.javastreets.mulefd.drawings.DiagramType;

import picocli.CommandLine;

@CommandLine.Command(name = "graph", header = "Create beautiful graph diagrams for mule flows.")
public class Graph extends DiagramBaseCommand {
  @Override
  protected DiagramType getDiagramType() {
    return DiagramType.GRAPH;
  }

}
