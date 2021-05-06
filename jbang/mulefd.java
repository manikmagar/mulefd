///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavencentral, jitpack
//JAVA 8+
//DEPS com.github.manikmagar:mulefd:v0.7.4

import com.javastreets.mulefd.app.Application;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

class mulefd {

  public static void main(String... args) {
    Application.main(args);
  }
}