package com.proxy.kiwi.app;

import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.Instancer;
import com.proxy.kiwi.core.services.KiwiInstancer;
import com.proxy.kiwi.core.services.Thumbnails;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.utils.Resources;
import com.proxy.kiwi.core.utils.Stopwatch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;

import org.apache.tools.ant.util.JavaEnvUtils;

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
		try {
			SystemTray tray = SystemTray.getSystemTray();

			if (tray.getTrayIcons().length == 0) {
				javafx.scene.image.Image IMG = new javafx.scene.image.Image(
						Resources.get("kiwi_small.png").openStream());

				Image img = SwingFXUtils.fromFXImage(IMG, null);

				int trayIconWidth = new TrayIcon(img).getSize().width;

				TrayIcon icon = new TrayIcon(img.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));
				icon.addActionListener(event -> {
					KiwiInstancer instancer = new KiwiInstancer();
					instancer.resume(Instancer.SELF_WAKE);
				});

				MenuItem showItem = new MenuItem("Show");
				showItem.addActionListener(event -> {
					KiwiInstancer instancer = new KiwiInstancer();
					instancer.resume(Instancer.SELF_WAKE);
				});

				MenuItem hideItem = new MenuItem("Hide");
				hideItem.addActionListener(event -> {
					Platform.runLater(() -> {
						stage.hide();
						exit();
					});
				});

				MenuItem exitItem = new MenuItem("Exit");
				exitItem.addActionListener(event -> {
					KiwiInstancer instancer = new KiwiInstancer();
					instancer.shutdown();
				});

				PopupMenu popup = new PopupMenu();

				popup.add(showItem);
				popup.add(hideItem);
				popup.add(exitItem);

				File fontFile = Resources.getFile("fonts/Ubuntu-L.ttf", "font");

				popup.setFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontFile).deriveFont(16f));

				icon.setPopupMenu(popup);

				tray.add(icon);
			}

		} catch (Exception e) {
			Log.print(e);
		}

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
