package com.javastreets.mulefd.app;

import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {
  @Override
  public String[] getVersion() throws Exception {
    return new String[] {com.javastreets.mulefd.app.BuildConfig.VERSION};
  }
}
