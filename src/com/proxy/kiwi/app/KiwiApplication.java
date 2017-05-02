package com.proxy.kiwi.app;

import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;

import org.apache.tools.ant.util.JavaEnvUtils;

import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.Instancer;
import com.proxy.kiwi.core.services.KiwiInstancer;
import com.proxy.kiwi.core.services.Thumbnails;
import com.proxy.kiwi.core.utils.Resources;
import com.proxy.kiwi.core.utils.Stopwatch;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public abstract class KiwiApplication extends Application {

	@Override
	public void start(Stage stage) throws Exception {

		KiwiInstancer.setStage(stage);

		Platform.setImplicitExit(false);
		Config.init();
		Thumbnails.init();

		initialize(stage);

		Resources.getAll("fonts/Ubuntu-L.ttf", "fonts/Ubuntu-B.ttf").forEach((font) -> {
			Font.loadFont(font, 14);
		});

		stage.getScene().getStylesheets().addAll(Resources.getAll("application.css"));

		stage.setOnCloseRequest((e) -> {
			exit();
		});

		Platform.runLater(() -> {
			addTrayIcon(stage);
		});
	}

	protected abstract void initialize(Stage stage);

	public static void exit() {
		Config.save();
		KiwiInstancer instancer = new KiwiInstancer();

		try {
			instancer.sleep();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OverlappingFileLockException e) {
			/*
			 * Nothing to do, since the file tried to re-lock itself. This means
			 * that the program was already sleeping.
			 */
		}
	}

	public static void addTrayIcon(Stage stage) {
		SystemTray tray = SystemTray.get();
		
		if (tray == null) {
			return;
		}
		
		tray.setTooltip("Kiwi");
		tray.setImage(Resources.get("kiwi_small.png"));

		tray.getMenu().setCallback( (e) -> {
			KiwiInstancer instancer = new KiwiInstancer();
			instancer.resume(Instancer.SELF_WAKE);
		});
		
		tray.getMenu().add(new MenuItem("Show", (e)-> {
			KiwiInstancer instancer = new KiwiInstancer();
			instancer.resume(Instancer.SELF_WAKE);
		}));

		tray.getMenu().add(new MenuItem("Hide" , (e) -> {
			Platform.runLater(() -> {
				stage.hide();
				exit();
			});			
		}));

		tray.getMenu().add(new MenuItem("Exit", (e) -> {
			KiwiInstancer instancer = new KiwiInstancer();
			instancer.shutdown();			
		}));
	}

	public static void startReader(String file) {
		String classpath = System.getProperty("java.class.path");
		String path = JavaEnvUtils.getJreExecutable("java");

		ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, Kiwi.class.getName(), file);

		processBuilder.inheritIO();

		try {
			Config.save();

			processBuilder.start();
			Stopwatch.click("Starting new JVM");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
