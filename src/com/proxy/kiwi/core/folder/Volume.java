package com.proxy.kiwi.core.folder;

import java.io.File;

public class Volume implements Comparable<Volume> {

	private String name, path;
	private File file;

	public Volume(String name, String path, Folder folder) {
		this.name = name;
		this.path = path;
		this.file = new File(path);
	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return path;
	}

	public File getFile() {
		return file;
	}

	@Override
	public int compareTo(Volume other) {
		return getName().compareTo(other.getName());
	}
}
