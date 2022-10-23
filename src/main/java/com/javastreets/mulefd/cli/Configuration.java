package com.javastreets.mulefd.cli;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
  protected final Properties properties = new Properties();

  public static final String MULEFD_DOT_DIRECTORY = ".mulefd";

  public static final String MULEFD_CONFIG_PROPERTIES = "mulefd.properties";

  private static final Logger log = LoggerFactory.getLogger(Configuration.class);

  public boolean containsKey(String key) {
    return properties.containsKey(key);
  }

  public String getValue(String key) {
    String value;
    if (containsKey(key)) {
      value = Objects.toString(properties.get(key));
    } else {
      value = null;
    }
    return value;
  }

  public String getValue(String key, String defaultValue) {
    return Objects.toString(properties.get(key), defaultValue);
  }

  public static Configuration readUserHomeConfig() {
    String userHome = System.getProperty("user.home");
    Path configProperties =
        Paths.get(userHome).resolve(MULEFD_DOT_DIRECTORY).resolve(MULEFD_CONFIG_PROPERTIES);
    log.info("Loading default properties file from: {}", configProperties);
    return read(configProperties);
  }

  public static Configuration read(Path configFile) {
    Configuration configuration = new Configuration();
    if (Files.isRegularFile(configFile)) {
      try (Reader in = Files.newBufferedReader(configFile)) {
        Properties props = new Properties();
        props.load(in);
        configuration.properties.putAll(props);
      } catch (IOException e) {
        log.warn("Couldn't parse configuration: {}", configFile);
      }
    }
    return configuration;
  }
}
