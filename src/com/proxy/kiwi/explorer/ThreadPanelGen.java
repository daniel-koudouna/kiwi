package com.proxy.kiwi.explorer;

import com.proxy.kiwi.core.folder.Folder;

import javafx.application.Platform;
import javafx.scene.layout.FlowPane;

import java.util.Queue;

public class ThreadPanelGen extends Thread {
	Queue<Folder> folderQueue;
	FlowPane pane;
	KiwiExplorerPane controller;

	public ThreadPanelGen(Queue<Folder> folderQueue, FlowPane flowPane, KiwiExplorerPane controller) {
		this.folderQueue = folderQueue;
		this.pane = flowPane;
		this.controller = controller;
	}

	public void run() {
		int size = folderQueue.size();
		int processed = 0;

		while (!folderQueue.isEmpty()) {
			Folder f = folderQueue.remove();

			processed++;
			double percent = (1.0 * processed) / (1.0 * size);

			Platform.runLater(() -> {
				FolderPanel p = new FolderPanel(f, controller);

				pane.getChildren().add(p);
				controller.loadingBar.setProgress(percent);
				controller.loadingLabel.setText(f.getFilenameProperty().get());
				controller.updateLast();

				if (percent == 1) {
					controller.loadingBox.setVisible(false);
				}
			});

			try {
				sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
