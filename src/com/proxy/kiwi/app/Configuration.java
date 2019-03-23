package com.proxy.kiwi.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.scene.input.KeyCode;

public class Configuration {

  public ArrayList<String> paths;
  public int viewer_scroll_delta;
  public int viewer_translate_delta;
  public int viewer_idle_time;
  public int viewer_cache_size;

  private ArrayList<String> keys_left, keys_right;
  private ArrayList<String> keys_prev, keys_next;
  private ArrayList<String> keys_up, keys_down;
  private ArrayList<String> keys_fullscreen, keys_exit;
  private ArrayList<String> keys_minus, keys_plus;
  private ArrayList<String> keys_minimize;

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

    this.viewer_scroll_delta = 8;
    this.viewer_translate_delta = 150;
    this.viewer_idle_time = 1500;
    this.viewer_cache_size = 8;
    this.viewer_cache_size = 8;

    this.keys_left = listOf("A","K","LEFT");
    this.keys_right = listOf ("D","J","RIGHT");
    this.keys_prev = listOf ("H");
    this.keys_next = listOf ("L");
    this.keys_minimize = listOf("M");
    this.keys_fullscreen = listOf("F");
    this.keys_exit = listOf("X");
    this.keys_up = listOf("W", "UP");
    this.keys_down = listOf ("S", "DOWN");
    this.keys_minus = listOf ("MINUS");
    this.keys_plus = listOf ("EQUALS", "PLUS");

    setTimestamp();
  }

  public ArrayList<String> listOf(String...s) {
    return new ArrayList<String>(Arrays.asList(s));
  }

  public Action actionFor(KeyCode c) {
    Function<ArrayList<String>,Boolean> in =
      (arr -> arr.stream().map(KeyCode::valueOf).anyMatch(c::equals));

    if (in.apply(keys_up))
      return Action.UP;
    if (in.apply(keys_down))
      return Action.DOWN;
    if (in.apply(keys_left))
      return Action.LEFT;
    if (in.apply(keys_right))
      return Action.RIGHT;
    if (in.apply(keys_prev))
      return Action.PREVIOUS;
    if (in.apply(keys_next))
      return Action.NEXT;
    if (in.apply(keys_exit))
      return Action.EXIT;
    if (in.apply(keys_fullscreen))
      return Action.FULL_SCREEN;
    if (in.apply(keys_minus))
      return Action.ZOOM_OUT;
    if (in.apply(keys_plus))
      return Action.ZOOM_IN;
    if (in.apply(keys_minimize))
      return Action.MINIMIZE;

    return Action.NONE;
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
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
