package com.javastreets.mulefd.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "version", description = "Display version info.")
public class Version extends BaseCommand {

  @Override
  protected Integer execute() {
    System.out.println(VersionProvider.getMuleFDVersion());
    return EXIT_OK;
  }


  public static class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
      return new String[] {getMuleFDVersion()};
    }

    public static String getMuleFDVersion() {
      return com.javastreets.mulefd.app.BuildConfig.VERSION;
    }
  }
}
