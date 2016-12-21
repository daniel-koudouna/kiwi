package com.proxy.kiwi.core.folder;

import java.io.File;
import java.util.Arrays;

public class FileFolder extends Folder {

	public FileFolder(String name, String path, Folder parent) {
		super(name, path, parent);
	}

	@Override
	public void load() {

		isLoaded.set(true);

		if (hasZipFolderParent()) {
			getZipParent().load(this);
		} else if (hasRarFolderParent()) {
			getRarParent().load(this);
		} else {
			File file = new File(getFilenameProperty().get());
			File[] files = file.listFiles();
			Arrays.sort(files,FileComparators.WINDOWS_LIKE);
			getVolumes().clear();
			for (File child : files) {
				if (Type.getType(child) == Type.IMAGE) {
					addVolume(new Volume(child.getName(), child.getPath(), this));
				}
			}
		}

	}

	@Override
	protected File[] build() {
		File file = new File(filename.get());
		return file.listFiles();
	}

	@Override
	protected String filterName(String name) {
		return name.replaceAll("(\\(.*?\\))|(\\[.*?\\])|(\\{.*?\\})|(=.*?=)|(~.*?~)", "").replaceAll("\\A\\s*", "")
				.trim();
	}

}
