package com.proxy.kiwi.core.services;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Optional;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.folder.Folders;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.utils.Stopwatch;
import com.proxy.kiwi.explorer.KiwiExplorerPane;
import com.proxy.kiwi.reader.KiwiReadingPane;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.stage.Stage;

//TODO modernize
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

			Parent parent = stage.getScene().getRoot();

			Dimension2D sizeDim = new Dimension2D(stage.getWidth(), stage.getHeight());
			Dimension2D posDim = new Dimension2D(stage.getX(), stage.getY());

			boolean wasFull = stage.isFullScreen();

			if (parent instanceof KiwiReadingPane) {

				KiwiReadingPane pane = ((KiwiReadingPane) parent);

				Optional<Folder> newFolder = Folder.fromFile(input);

				newFolder.ifPresent(folder -> {
					pane.setFolder(folder);
					pane.changePage(folder.getStartPage());
				});


			} else if (parent instanceof KiwiExplorerPane) {

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
		Folders.getTempFiles().parallelStream().forEach(f -> {
			deleteDir(f);
		});
		System.exit(0);
	}

	static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
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
