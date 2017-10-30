package com.proxy.kiwi.core.services;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.image.Cache;
import com.proxy.kiwi.core.image.KiwiImage;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.utils.Resources;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class Thumbnails {
	private static Image loading;
	private static Cache thumbnailCache = new Cache(2000);

	public static int thumb_width, thumb_height;

	private static LinkedBlockingQueue<Folder> requestQueue = new LinkedBlockingQueue<>();
	private static LinkedBlockingQueue<Folder> expressQueue = new LinkedBlockingQueue<>();

	private static ThumbnailThread thread;
	static volatile boolean alive;

	public static void init() {
		alive = true;
		thumb_width = Config.getIntOption("item_width");
		thumb_height = Config.getIntOption("item_height");
		thread = new ThumbnailThread();
		thread.start();

		Platform.runLater(() -> {
			loading = new Image(Resources.get("loading.gif").toString());
		});

	}

	public static void stop() {
		alive = false;
	}

	@Deprecated
	public static void pause() {
		thread.suspend();
	}

	@Deprecated
	public static void resume() {
		thread.resume();
	}

	public static Image getLoading() {
		return loading;
	}

	public static Cache getCache() {
		return thumbnailCache;
	}

	public synchronized static LinkedBlockingQueue<Folder> getRequests() {
		return requestQueue;
	}

	public synchronized static LinkedBlockingQueue<Folder> getExpress() {
		return expressQueue;
	}

//	public static void request(Folder folder) {
//		requestQueue.add(folder);
//	}

	public static void requestExpress(Folder folder) {
		expressQueue.add(folder);
	}

	public static void requestExpressOverwrite(Folder folder) {
		expressQueue.clear();
		folder.children().forEach(expressQueue::add);
	}
}

class ThumbnailThread extends Thread {
	@Override
	public void run() {

		Folder folder;

		while ((Thumbnails.alive)) {
			try {
				folder = Thumbnails.getExpress().poll(100, TimeUnit.MILLISECONDS);
				if (folder != null) {
					process(folder, true);
					continue;
				}
				// folder =
				// Thumbnails.getRequests().poll(100,TimeUnit.MILLISECONDS);
				// if (folder != null) { process(folder,false); }
			} catch (InterruptedException e) {
				Log.print(e);
			}
		}
	}

	public void process(Folder folder, boolean backgroundLoad) {
		String filename = Config.getFolderImage(folder);

		if (filename != null && !Thumbnails.getCache().contains(filename)) {

			int targetW = Thumbnails.thumb_width;
			int targetH = Thumbnails.thumb_height;

			KiwiImage image = null;

			double canvasRatio = targetW / (1.0 * targetH);

			/**
			 * Create a dummy version of the file to check the proportions. Then
			 * use the target ratio to load the correct sized image.
			 */
			Image dummy = new Image("file:" + filename, targetW, targetH, true, false);

			double w = dummy.getWidth();
			double h = dummy.getHeight();

			double scalingRatio;

			if (w / h > canvasRatio) {
				/* Crop width, constrain height */
				scalingRatio = targetH / h;
			} else {
				/* Crop height, constrain width */
				scalingRatio = targetW / w;
			}

			image = new KiwiImage(new File(filename), (int) (scalingRatio * w), (int) (scalingRatio * h), true, true,
					backgroundLoad);

			Thumbnails.getCache().add(filename, image);

			dummy = null;

		}
	}
}
