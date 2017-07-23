package com.proxy.kiwi.core.v2.folder;

import java.io.File;
import java.util.Optional;

public abstract class Item {
	protected final File file;
	protected String name;
	protected Optional<FolderV2> parent;
	
	public Item(File file, String name, FolderV2 parent) {
		super();
		this.file = file;
		this.name = name;
		this.parent = Optional.ofNullable(parent);
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public Optional<FolderV2> getParent() {
		return parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(FolderV2 folder) {
		this.parent = Optional.of(folder);
	}

	
	public static Optional<Item> from(File file, FolderV2 parent, File initial) {
		switch (ItemType.get(file)) {
		case FOLDER:
			return Optional.of(new FileFolderV2(file, file.getName(), parent, initial));
		case IMAGE:
			return Optional.of(new FolderImage(file,file.getName(),parent));
		case ZIP:
			return Optional.of(new ZipFolderV2(file, file.getName(), parent, initial));
		case SZ:
		case TAR:
		case UNKNOWN:
		default:
			return Optional.empty();
		}
	}
}
