package com.javastreets.mulefd.drawings.engine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;

public class GraphvizEngineHelper {

  private GraphvizEngineHelper() {

  }

  private static void initEngines(GraphvizEngine... moreEngines) {
    if (moreEngines == null) {
      Graphviz.useDefaultEngines();
    } else {
      Graphviz.useEngine(Arrays.asList(moreEngines));
    }
  }

  public static boolean generate(MutableGraph graph, Format format, File outputFilename)
      throws IOException {
    initEngines();
    boolean generated = Graphviz.fromGraph(graph).render(format).toFile(outputFilename).exists();
    Graphviz.releaseEngine();
    return generated;
  }

  public static String generate(MutableGraph graph, Format format) {
    // Skips using DOT engine. Used for tests to compare generated diagrams.
    initEngines(new GraphvizV8Engine(), new GraphvizJdkEngine());
    String graphGen = Graphviz.fromGraph(graph).render(format).toString();
    Graphviz.releaseEngine();
    return graphGen;
  }
}
