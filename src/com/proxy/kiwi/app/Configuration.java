package com.proxy.kiwi.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Optional;

import com.google.gson.Gson;

public class Configuration {

  public ArrayList<String> paths;
  public int viewer_scroll_delta = 6;
  public int viewer_translate_delta = 150;
  public int viewer_idle_time = 1500;
  public int viewer_cache_size = 8;

  private long timestamp;

  private static Path CONFIG_PATH;
  static {
    Path dir = Paths.get(System.getProperty("user.home"),".kiwi");
    dir.toFile().mkdir();
    CONFIG_PATH = Paths.get(dir.toString(),"config.json");
  }
  private static String OS = System.getProperty("os.name").toLowerCase();
  public static boolean isWindows() {
    return OS.contains("win");
  }

  private Configuration() {
    this.paths = new ArrayList<>();
    this.paths.add(Paths.get(System.getProperty("user.home"),"Pictures").toString());
    setTimestamp();
  }

  public void setTimestamp() {
    this.timestamp = System.nanoTime();
  }

  public static Configuration load() {
    return Configuration.read().orElse(new Configuration());
  }

  public void save() {
    Optional<Configuration> savedConfig = Configuration.read();

    boolean override = true;
    if (savedConfig.isPresent()) {
      override = savedConfig.get().timestamp < this.timestamp;
    }

    if (override) {
      write();
    }
  }

  private void write() {
    Gson gson = new Gson();
    String file = gson.toJson(this);
    try {
      Files.write(CONFIG_PATH, file.getBytes(), StandardOpenOption.TRUNCATE_EXISTING,
		  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static Optional<Configuration> read() {
    Gson gson = new Gson();
    try {
      String file = Files.lines(CONFIG_PATH).reduce("", (a, b) -> a + "\n" + b);
      return Optional.of(gson.fromJson(file, Configuration.class));
    } catch (IOException e) {
      // e.printStackTrace();
      System.err.println("Error while reading configuration file.");
      return Optional.empty();
    }
  }
}
