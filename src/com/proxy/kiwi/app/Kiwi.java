package com.proxy.kiwi.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.proxy.kiwi.instancer.LaunchParameters;
import com.proxy.kiwi.ui.AbstractController;
import com.proxy.kiwi.ui.Viewer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Kiwi extends Application {
  public static URL resource(String path) {
    return Kiwi.class.getResource("/com/proxy/kiwi/res/" + path);
  }

  private Optional<LaunchParameters> readParams(Path path) {
    LaunchParameters parameters = null;
    try {
      File infile = path.toFile();
      FileInputStream fis = new FileInputStream(infile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      parameters = (LaunchParameters) ois.readObject();
      ois.close();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Optional.ofNullable(parameters);
  }


  private Optional<AbstractController> getController(Stage stage, Configuration config) {
    List<String> params = getParameters().getUnnamed();
    if (params.size() == 1 && params.get(0).endsWith(".tmp")) {
      System.out.println("READING TEMP FILE");

      return readParams(Paths.get(params.get(0)))
	.map(parameters -> {
	    Path initialPath = Paths.get(parameters.initial);
	    parameters.nodelist.forEach(in -> in.build());
	    return new Viewer(parameters.nodelist, initialPath, stage, config);
	  });
    } else {
      return Parameter.controller(stage,params,config);
    }
  }

  @Override
  public void start(Stage stage) throws Exception{
    long then = System.nanoTime();

    Configuration configuration = Configuration.load();

    stage.setTitle("Kiwi");

    Optional<AbstractController> c = getController(stage, configuration);
    if (!c.isPresent()) {
      System.exit(1);
    }

    AbstractController controller = c.get();
    stage.setScene(new Scene(controller.component(),700, 700));
    stage.setOnCloseRequest((e) -> {
	configuration.save();
	controller.exit();
      });

    stage.getScene().getStylesheets().add(Kiwi.resource("application.css").toString());
    stage.show();

    long now = System.nanoTime();
    System.out.println("Startup Time " + ((now - then)/1_000_000_000.0) + " s");
  }


  public static void main(String[] args) {
    launch(args);
  }
}
