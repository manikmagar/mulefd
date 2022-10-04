package com.javastreets.mulefd.drawings.engine;

import java.io.File;
import java.io.IOException;

import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;

public class GraphvizEngineHelper {

  private static void initEngines() {
    // See https://github.com/nidi3/graphviz-java#how-it-works for which engines are used.
    // Known Limitations:
    // 1. J2V8 engines isn't available for Apple M1
    // 2. Java Nashorn engine is deprecated by JDK and removed in JDK 15.
    // Changing the engine order here to retain previous behaviors.
    Graphviz.useEngine(new GraphvizV8Engine(), new GraphvizJdkEngine(),
        new GraphvizCmdLineEngine());
  }

  public static boolean generate(MutableGraph graph, Format format, File outputFilename)
      throws IOException {
    initEngines();
    boolean generated = Graphviz.fromGraph(graph).render(format).toFile(outputFilename).exists();
    Graphviz.releaseEngine();
    return generated;
  }

  public static String generate(MutableGraph graph, Format format) {
    initEngines();
    String graphGen = Graphviz.fromGraph(graph).render(format).toString();
    Graphviz.releaseEngine();
    return graphGen;
  }
}
