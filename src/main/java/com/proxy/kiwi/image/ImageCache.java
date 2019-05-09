package com.proxy.kiwi.image;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import com.proxy.kiwi.utils.Log;
import com.proxy.kiwi.utils.Tuple;

import javafx.scene.image.Image;

public class ImageCache {

  LinkedList<Tuple<Path, Image>> cache;
  Queue<Path> queue;
  private int capacity;

  public ImageCache(int capacity) {
    this.cache = new LinkedList<>();
    this.queue = new LinkedList<>();
    this.capacity = capacity;
  }

  public void start() {
    new Thread(() -> {
	while (true) {
	  if (!queue.isEmpty()) {
	    Path p;
	    synchronized (queue) {
	      p = queue.poll();
	    }
	    if (cache.size() > this.capacity) {
	      cache.removeFirst();
	    }
	    try {
	        String path = p.toUri().toURL().toExternalForm();
	        KMetadata data = new KMetadata(p);
	        Log.info(ImageCache.class,"Reading image with dimensions " + data.width  + " x " + data.height);
	        if (data.height > 4000 || data.width > 4000) {
	            Log.debug(ImageCache.class, "Resizing large image");
                cache.add(new Tuple<>(p, new Image(path, -1, data.height/4.0, true, true)));
	        } else {
	            cache.add(new Tuple<>(p, new Image(path)));
	        }
	    } catch (MalformedURLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    } catch (OutOfMemoryError e) {
	        e.printStackTrace();
	        cache.clear();
	        if (capacity > 1) {
	            capacity--;
	        }
	    }
	  } else {
	    try {
	      Thread.sleep(10);
	    } catch (InterruptedException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	  }
	}
    }).start();
  }

  public boolean containsQueued(Path path) {
    return queue.contains(path) || containsCached(path);
  }

  public boolean containsCached(Path path) {
    return cache.stream()
      .anyMatch(t -> t.x.equals(path));

  }

  public void put(Path path) {
    if (!containsQueued(path)) {
      synchronized (queue) {
	queue.add(path);
      }

    }
  }

  public Optional<Image> get(Path path) {
    return cache.stream()
      .filter(t -> t.x.equals(path))
      .map(t -> t.y)
      .findFirst();
  }

  public void show() {
    Log.list(ImageCache.class, Log::info, cache.stream().map(t -> t.x), "Cache");
  }
}
