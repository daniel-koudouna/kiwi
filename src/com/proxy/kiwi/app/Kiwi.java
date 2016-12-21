package com.proxy.kiwi.app;

import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.KiwiInstancer;
import com.proxy.kiwi.core.utils.Resources;
import com.proxy.kiwi.core.utils.Stopwatch;
import com.proxy.kiwi.explorer.KiwiExplorerPane;
import com.proxy.kiwi.reader.KiwiReadingPane;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Kiwi extends KiwiApplication {

	public static void main(String[] args) {
		if (args.length != 0) {
			KiwiInstancer instancer = new KiwiInstancer();

			try {
				boolean woke = instancer.wakeIfExists(args);
				if (woke) {
					System.exit(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}

		launch(args);
	}

	protected void initialize(Stage stage) {
		List<String> params = getParameters().getUnnamed();

		Scene scene;
		
		if (params.size() > 0) {
			Stopwatch.click("Loading reader");

			String path = params.get(0);

			scene = new Scene(new KiwiReadingPane(stage, path));

			Stopwatch.click("Loading reader");
		} else {
			Stopwatch.click("Loading explorer");
			
			scene = new Scene(new KiwiExplorerPane(stage, Config.getOption("path")));

			Stopwatch.click("Loading explorer");
		}
		
		stage.getIcons().add(new Image(Resources.get("kiwi_small.png").toString()));
		stage.setScene(scene);
		stage.show();
	}

}
