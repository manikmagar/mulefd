package com.javastreets.mulefd.cli;

import java.util.concurrent.Callable;

import picocli.CommandLine;

public abstract class BaseCommand implements Callable<Integer> {

  public static final int EXIT_OK = 0;
  @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true,
      description = "display version info")
  boolean versionInfoRequested;

  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
      description = "display this help message")
  boolean usageHelpRequested;

  @Override
  public Integer call() throws Exception {
    return execute();
  }

  protected abstract Integer execute();

}
