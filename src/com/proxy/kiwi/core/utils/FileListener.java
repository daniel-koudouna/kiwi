package com.proxy.kiwi.core.utils;

import java.io.File;

public class FileListener {

	private long lastModified;
	
	public static FileListener create(String path, Runnable onChange) {
		return new FileListener(path, onChange);
	}
	
	public FileListener(String path, Runnable onChange) {
		this.lastModified = new File(path).lastModified();
		
		new Thread( ()-> {
			while (true) {
				try {
					Thread.sleep(1000);
					File file = new File(path);
					long newTime = file.lastModified();
					if (newTime > lastModified) {
						onChange.run();
						this.lastModified = newTime;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
