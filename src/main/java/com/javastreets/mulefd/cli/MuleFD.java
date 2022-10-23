package com.javastreets.mulefd.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "mulefd",
    footer = "\nCopyright: 2020-2022 Manik Magar, License: MIT\nWebsite: https://github.com/manikmagar/mulefd",
    showDefaultValues = true, versionProvider = Version.VersionProvider.class,
    subcommands = {CommandLine.HelpCommand.class, Version.class, Graph.class},
    header = "${COMMAND-NAME} is a tool to generate beautiful diagrams for your mule application flows.",
    description = {"", " Some command examples:", " - ${COMMAND-NAME} graph <MULE_APP_PATH>",
        "       Generate a Graph diagram for mule configuration in mule app",
        " - ${COMMAND-NAME} help graph", "       See help for Graph generation", "",})
public class MuleFD extends BaseCommand {

  @CommandLine.Spec
  CommandLine.Model.CommandSpec spec;

  @Override
  protected Integer execute() {
    spec.commandLine().usage(System.err);
    return EXIT_OK;
  }

  public static CommandLine getCommandLine() {
    CommandLine cl = new CommandLine(new MuleFD());
    cl.setParameterExceptionHandler(
        new CommandMoveMessageHandler(cl.getParameterExceptionHandler()));
    return cl;
  }

  public static class CommandMoveMessageHandler implements CommandLine.IParameterExceptionHandler {
    private final CommandLine.IParameterExceptionHandler delegate;

    public CommandMoveMessageHandler(CommandLine.IParameterExceptionHandler delegate) {
      this.delegate = delegate;
    }

    @Override
    public int handleParseException(CommandLine.ParameterException ex, String[] args)
        throws Exception {
      if (ex instanceof CommandLine.UnmatchedArgumentException) {
        CommandLine commandLine = ex.getCommandLine();
        commandLine.getErr().println(
            "MuleFD diagram generation is now moved to diagram type specific sub-commands. See `mulefd help` for usage details.");
        commandLine.getErr().println();
      }
      return delegate.handleParseException(ex, args);
    }
  }
}
