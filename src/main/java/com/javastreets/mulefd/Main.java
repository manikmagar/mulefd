package com.javastreets.mulefd;

import com.javastreets.mulefd.cli.MuleFD;

public class Main {

  public static void main(String[] args) {
    int exitCode = MuleFD.getCommandLine().execute(args);
    System.exit(exitCode);
  }

}
