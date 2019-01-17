package com.proxy.kiwi.app;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class Configuration {

  public ArrayList<String> paths;
  public int viewer_scroll_delta = 6;
  public int viewer_translate_delta = 150;
  public int viewer_idle_time = 1500;
  public int viewer_cache_size = 8;

  //private long timestamp;

  private static String OS = System.getProperty("os.name").toLowerCase();
  public static boolean isWindows() {
    return OS.contains("win");
  }
  private Configuration() {
    this.paths = new ArrayList<>();
    this.paths.add(Paths.get(System.getProperty("user.home"),"Pictures").toString());
    //this.timestamp = System.nanoTime();
  }

  public static Configuration load() {
    return Configuration.read().orElse(new Configuration());
  }

  public void save() {
    write();
  }

  private void write() {

  }

  private static Optional<Configuration> read() {
    return Optional.empty();
  }
}
