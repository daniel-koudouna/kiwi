package com.proxy.kiwi.core.services;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.utils.Stopwatch;
import com.proxy.kiwi.reader.KiwiReadingPane;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.channels.FileLock;

public class KiwiInstancer extends Instancer {

	private static Stage stage;
	private static FileLock lock;

	public static void setStage(Stage stage) {
		KiwiInstancer.stage = stage;
	}

	@Override
	public void pause(FileLock lock) {
		KiwiInstancer.lock = lock;
		Log.print(Log.EVENT, "Sleeping!");
	}

	@Override
	public void resume(String input) {
		if (input.equals(Instancer.SELF_WAKE)) {
			try {
				lock.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.print(Log.EVENT, "Woken up!\t\t(" + input + ")");
		Stopwatch.click("Resuming");
		Platform.runLater(() -> {

			KiwiReadingPane pane = (KiwiReadingPane) stage.getScene().getRoot();

			Dimension2D sizeDim = new Dimension2D(stage.getWidth(), stage.getHeight());
			Dimension2D posDim = new Dimension2D(stage.getX(), stage.getY());

			boolean wasFull = stage.isFullScreen();

			Folder folder = Folder.fromFile(input);
			if (folder != null) {
				folder.load();

				pane.setFolder(folder);
				pane.changePage(Folders.find(folder, input));

			}

			stage.setWidth(sizeDim.getWidth());
			stage.setHeight(sizeDim.getHeight());

			stage.setX(posDim.getWidth());
			stage.setY(posDim.getHeight());

			stage.setFullScreen(wasFull);

			stage.show();
			Stopwatch.click("Resuming");
		});
	}

	@Override
	public void shutdown() {
		System.exit(0);
	}

	@Override
	protected String getLockName() {
		return "kiwi.lock";
	}

	@Override
	protected String getDataName() {
		return "kiwi.data";
	}
}
