package com.proxy.kiwi.core.folder;

import java.io.File;
import java.util.Optional;

public abstract class Item {
	protected final File file;
	protected String name;
	protected Optional<Folder> parent;

	public Item(File file, String name, Folder parent) {
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

	public Optional<Folder> getParent() {
		return parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Folder folder) {
		this.parent = Optional.of(folder);
	}


	public static Optional<Item> from(File file, Folder parent, File initial) {
		try {
			switch (ItemType.get(file)) {
			case FOLDER:
				return Optional.of(new FileFolder(file, file.getName(), parent, initial));
			case IMAGE:
				return Optional.of(new FolderImage(file,file.getName(),parent));
			case ZIP:
				return Optional.of(new ZipFolder(file, file.getName(), parent, initial));
			case SZ:
			case TAR:
			case UNKNOWN:
			default:
				return Optional.empty();
			}
		} catch (Exception e) {
			System.err.println("Encountered Exception while opening " + file + " :\n " + e.getMessage());
			return Optional.empty();
		}

	}
}
