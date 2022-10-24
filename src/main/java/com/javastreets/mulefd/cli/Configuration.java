package com.javastreets.mulefd.cli;

import java.io.*;
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

  private static final Configuration globalConfig = readUserHomeConfig();
  private static final Configuration defaultConfig = readDefaultConfig();

  private static final Configuration mergedConfig = mergedConfig();

  /**
   * Configuration properties loaded from ${user.home}/.mulefd/mulefd.properties
   * 
   * @return Configuration
   */
  public static Configuration getGlobalConfig() {
    return globalConfig;
  }

  /**
   * Configuration properties loaded from inbuilt properties file.
   * 
   * @return Configuration
   */
  public static Configuration getDefaultConfig() {
    return defaultConfig;
  }

  /**
   * <pre>
   * Merge available properties files to consolidate into one. Order of precedence is -
   *  - Default properties
   *  - Global properties
   * </pre>
   * 
   * @return Configuration
   */
  public static Configuration getMergedConfig() {
    return mergedConfig;
  }

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

  public void printConfiguration(PrintStream out) {
    properties.list(out);
  }

  /**
   * Merges the default configuration properties file with global configuration from user's home
   * directory.
   * 
   * @return Configuration
   */
  private static Configuration mergedConfig() {
    Configuration configuration = new Configuration();
    defaultConfig.properties.stringPropertyNames()
        .forEach(p -> configuration.properties.setProperty(p, defaultConfig.getValue(p)));
    globalConfig.properties.stringPropertyNames()
        .forEach(p -> configuration.properties.setProperty(p, globalConfig.getValue(p)));
    return configuration;
  }

  /**
   * Reads a default, inbuilt configuration file.
   * 
   * @return Configuration
   */
  private static Configuration readDefaultConfig() {
    InputStream defaultConfigStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("default-" + MULEFD_CONFIG_PROPERTIES);
    Configuration configuration = new Configuration();
    try {
      configuration.properties.load(defaultConfigStream);
    } catch (IOException e) {
      throw new RuntimeException("Cannot load default config properties");
    }
    return configuration;
  }

  /**
   * Reads configuration file from ${user.home}/.mulefd/mulefd.properties
   * 
   * @return Configuration
   */
  private static Configuration readUserHomeConfig() {
    String userHome = System.getProperty("user.home");
    Path configProperties =
        Paths.get(userHome).resolve(MULEFD_DOT_DIRECTORY).resolve(MULEFD_CONFIG_PROPERTIES);
    if (!configProperties.toFile().exists())
      return new Configuration();
    log.debug("Loading default properties file from: {}", configProperties);
    return read(configProperties);
  }

  /**
   * Read a configuration file at give path.
   * 
   * @param configFile {@link Path} to config .properties file
   * @return Configuration
   */
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
