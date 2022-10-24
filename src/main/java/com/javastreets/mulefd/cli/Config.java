package com.javastreets.mulefd.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "config", description = "Manage MuleFD Global configuration",
    subcommands = {Config.ListConfig.class})
public class Config extends BaseCommand {

  @CommandLine.Spec
  CommandLine.Model.CommandSpec cli;

  @Override
  protected Integer execute() {
    cli.commandLine().usage(System.err);
    return EXIT_OK;
  }

  @CommandLine.Command(name = "list", description = "List all global configuration values")
  public static class ListConfig extends BaseCommand {

    @Override
    protected Integer execute() {
      Configuration configuration = Configuration.getMergedConfig();
      configuration.printConfiguration(System.out);
      return EXIT_OK;
    }
  }

}
