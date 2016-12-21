package com.proxy.kiwi.core.image;

import com.proxy.kiwi.core.utils.Log;

/**
 * Image cache for JavaFX image loading.
 * 
 * @author Daniel
 *
 */
public class Cache {
	private int size;

	private int index;

	String[] keys;
	KiwiImage[] images;

	public Cache(int size) {
		this.size = size;
		keys = new String[size];
		images = new KiwiImage[size];
		index = 0;
	}

	public void add(String key, KiwiImage value) {

		synchronized (keys) {

			if (images[index] != null) {
				images[index] = null;
				Log.print(Log.IO, "Removing to cache: " + keys[index]);
			}

			Log.print(Log.IO, "Adding to cache: " + key);

			keys[index] = key;
			images[index] = value;

			index = (index + 1) % size;
		}

		/* "Java can manage heap space just fine" */
		System.gc();
	}

	public boolean contains(String name) {
		for (String key : keys) {
			if (key != null && key.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public KiwiImage get(String key) {
		synchronized (keys) {
			for (int i = 0; i < size; i++) {
				if (keys[i].equals(key)) {
					return images[i];
				}
			}
		}
		return null;
	}

	public void clear() {
		for (int i = 0; i < size; i++) {
			keys[i] = "-1";
			images[i] = null;
		}
		System.gc();
	}
}
