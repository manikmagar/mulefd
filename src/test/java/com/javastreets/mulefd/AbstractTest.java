package com.javastreets.mulefd;

import java.io.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.javastreets.mulefd.util.ConsoleLog;

public abstract class AbstractTest {

  private TestPrintStream out;

  protected String getLogEntries() {
    return out.getCapturedEntries();
  }

  @BeforeEach
  public void setupLog() {
    out = new TestPrintStream();
    ConsoleLog.setOutStream(new PrintStream(out));
  }

  @AfterEach
  public void tearDown() throws IOException {
    out.close();
    ConsoleLog.defaultOutStream();
  }

  /**
   * An {@link OutputStream} to be wrapped with {@link PrintStream} of {@link ConsoleLog} for
   * capturing the logs and verify.
   *
   * See {@link #getCapturedEntries()} for accessing logs.
   */
  public static class TestPrintStream extends ByteArrayOutputStream {

    public String getCapturedEntries() {
      return this.toString();
    }

    @Override
    public synchronized void write(int b) {
      super.write(b);
      System.out.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
      super.write(b, off, len);
      System.out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
      super.close();
    }
  }
}
