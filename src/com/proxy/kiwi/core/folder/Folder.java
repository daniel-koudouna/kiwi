package com.proxy.kiwi.core.folder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.v2.folder.FileComparators;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public abstract class Folder {

	protected SimpleIntegerProperty size;
	protected SimpleStringProperty name, filename;
	protected SimpleBooleanProperty isLoaded;
	protected File file;

	private Folder parent;
	private Folder linkedFolder;
	protected ArrayList<Volume> volumes;
	private ArrayList<Folder> subfolders;
	private ArrayList<Folder> linkedFolders;

	public Folder(String name, String path, Folder parent) {
		this.name = new SimpleStringProperty(filterName(name));
		this.filename = new SimpleStringProperty(path);

		this.file = new File(path);

		this.size = new SimpleIntegerProperty(0);
		this.parent = parent;
		this.linkedFolder = parent;
		volumes = new ArrayList<>();
		subfolders = new ArrayList<>();
		linkedFolders = new ArrayList<>();
		if (hasRarFolderParent()) {
			getRarParent().getLinkedFolders().add(this);
		}
		if (hasZipFolderParent()) {
			getZipParent().getLinkedFolders().add(this);
		}
		isLoaded = new SimpleBooleanProperty(false);

		boolean containsImages = false;
		File[] files = build();
		Arrays.sort(files, FileComparators.WINDOWS_LIKE);

		if (files != null) {

			for (File child : files) {
				if (child != null) {
					Type type = Type.getType(child);
					switch (type) {
					case FOLDER:
						addFolder(new FileFolder(child.getName(), child.getPath(), this));
						break;
					case RAR:
//						addFolder(new RarFolder(child.getName(), child.getPath(), this));
						break;
					case ZIP:
						addFolder(new ZipFolder(child.getName(), child.getPath(), this));
						break;
					case IMAGE:
						containsImages = true;
						addVolume(new Volume(child.getName(), child.getPath(), this));
						break;
					default:
						break;
					}
				}
				if (containsImages) {
					break;
				}
			}

			if (!containsImages) {
				Log.print(Log.PRELOAD,
						"Finished building folder '" + getName() + "' with " + files.length + " objects.");
			}

			Collections.sort(subfolders, (first, second) -> {
				return first.filename.get().compareTo(second.filename.get());
			});
		}
	}

	/**
	 * Loads the image contents of the folder into volumes.
	 */
	public abstract void load();

	/**
	 * 
	 * @return a File[] with all the subfolders, and the minimum amount of
	 *         Volumes.
	 */
	// TODO replace with a better solution.
	protected abstract File[] build();

	protected abstract String filterName(String name);

	public void addFolder(Folder folder) {
		this.subfolders.add(folder);
	}

	public void addVolume(Volume volume) {
		volumes.add(volume);
		setSize(volumes.size());
	}

	public void clearVolumes() {
		volumes.clear();
		setSize(0);
	}

	/**
	 * Finds the appropriate image to display as a thumbnail for the Folder.
	 * 
	 * @return the image path of the appropriate image.
	 */
	public String getImagePath() {

		if (Config.getFolderImage(name.get()) != null) {
			// Search first in the settings.
			return Config.getFolderImage(name.get());
		} else if (hasVolumes()) {
			// Search the folder itself for volumes.
			return volumes.get(0).getFilename();
		} else if (hasSubfolders()) {
			// Search the subfolders for volumes.
			return subfolders.get(0).getImagePath();
		} else {
			return null;
		}
	}

	public SimpleIntegerProperty getSizeProperty() {
		return size;
	}

	public String getName() {
		return name.get();
	}

	public SimpleStringProperty getNameProperty() {
		return name;
	}

	public SimpleStringProperty getFilenameProperty() {
		return filename;
	}

	public SimpleBooleanProperty getIsLoadedProperty() {
		return isLoaded;
	}

	public Folder getParent() {
		return parent;
	}

	public ArrayList<Volume> getVolumes() {
		return volumes;
	}

	public ArrayList<Folder> getSubfolders() {
		return subfolders;
	}

	public ArrayList<Folder> getLinkedFolders() {
		return linkedFolders;
	}

	public boolean hasSubfolders() {
		return !subfolders.isEmpty();
	}

	public boolean hasVolumes() {
		return !volumes.isEmpty();
	}

	// TODO Find a better solution
	public boolean hasZipFolderParent() {
		return ((linkedFolder != null && linkedFolder instanceof ZipFolder)
				|| (parent != null && parent instanceof ZipFolder) || (parent != null && parent.hasZipFolderParent()));
	}

	// TODO Find a better solution
	public boolean hasRarFolderParent() {
		return ((linkedFolder != null && linkedFolder instanceof RarFolder)
				|| (parent != null && parent instanceof RarFolder) || (parent != null && parent.hasRarFolderParent()));
	}

	// TODO Find a better solution
	public ZipFolder getZipParent() {
		return (parent instanceof ZipFolder ? (ZipFolder) parent
				: (linkedFolder instanceof ZipFolder ? (ZipFolder) linkedFolder : parent.getZipParent()));
	}

	// TODO Find a better solution
	public RarFolder getRarParent() {
		return (parent instanceof RarFolder ? (RarFolder) parent
				: (linkedFolder instanceof RarFolder ? (RarFolder) linkedFolder : parent.getRarParent()));
	}

	public void setParent(Folder folder) {
		this.parent = folder;
	}

	public File getFile() {
		return file;
	}

	public void setSize(int n) {
		/**
		 * Properties always have to be changed in the JavaFX Thread.
		 */
		Platform.runLater(new Runnable() {
			public void run() {
				size.set(n);
			}
		});
	}

	/**
	 * @return true if the File is a direct child of this folder, false
	 *         otherwise.
	 */
	public boolean shouldContain(File file) {
		// Check if the file path contains the file path of the folder.
		if (file.getAbsolutePath().indexOf(filename.get()) > -1) {
			return true;
		} else {
			// Check instead if the folder name is the same (Direct child).
			return file.getParentFile().getName().indexOf(new File(filename.get()).getName()) > -1;
		}
	}

	public boolean hasAncestor(Folder parent) {
		return (this.parent != null) && (this.parent == parent || this.parent.hasAncestor(parent));
	}

	public boolean hasAncestorWith(String search) {
		return (this.parent != null) && (this.parent.getName().toLowerCase().contains(search) || this.parent.hasAncestorWith(search));
	}

	
	public boolean equals(Object other) {
		if (other == null || !other.getClass().getName().contains("Folder")) {
			return false;
		}
		return file.equals(((Folder) other).getFile());

	}

	public Folder previous() {
		File file = getFile();

		Folder parent = Folder.fromFile(file.getParentFile());

		// Folders.clean(parent);

		Iterator<Folder> iterator = parent.getSubfolders().iterator();

		Folder prev = null;
		while (iterator.hasNext()) {
			Folder f = iterator.next();
			if (f.equals(this)) {
				if (prev != null) {
					Folder fo = prev.getFirstImageFolder(false);
					if (fo != null) {
						fo.load();
						return fo;
					}
				}
			}
			prev = f;
		}

		return parent.previous();
	}

	public Folder next() {
		File file = getFile();

		Folder parent = Folder.fromFile(file.getParentFile());

		// Folders.clean(parent);
		boolean found = false;

		Iterator<Folder> iterator = parent.getSubfolders().iterator();

		while (iterator.hasNext()) {
			Folder f = iterator.next();
			if (found) {
				Folder fo = f.getFirstImageFolder(true);
				if (fo != null) {
					fo.load();
					return fo;
				}
			}
			if (f.equals(this)) {
				found = true;
			}
		}

		return parent.next();
	}

	private Folder getFirstImageFolder(boolean forward) {
		if (hasVolumes()) {
			return this;
		}
		if (forward) {
			for (Folder f : subfolders) {
				Folder sub = f.getFirstImageFolder(forward);
				if (sub != null) {
					return sub;
				}
			}

		} else {
			for (int i = subfolders.size() - 1; i >= 0; i--) {
				Folder f = subfolders.get(i);
				Folder sub = f.getFirstImageFolder(forward);
				if (sub != null) {
					return sub;
				}
			}
		}
		return null;
	}

	public int find(String absolutePath) {
		if (!getIsLoadedProperty().get()) {
			load();
		}

		for (int i = 0; i < getVolumes().size(); i++) {
			Volume v = getVolumes().get(i);
			if (v.getFilename().equals(filename)) {
				return i + 1;
			}
		}

		return 1;
	}

	/**
	 * ABSTRACT METHODS
	 */

	public static Folder fromFile(String path) {
		return fromFile(new File(path));
	}

	public static Folder fromFile(File file) {
		switch (Type.getType(file)) {
		case IMAGE:
			file = file.getParentFile();
			return new FileFolder(file.getName(), file.getAbsolutePath(), null);
		case OTHER:
			break;
		case RAR:
			return new RarFolder(file.getName(), file.getAbsolutePath(), null);
		case SZ:
			break;
		case TAR:
			break;
		case ZIP:
			return new ZipFolder(file.getName(), file.getAbsolutePath(), null);
		case FOLDER:
			return new FileFolder(file.getName(), file.getAbsolutePath(), null);
		default:
			break;

		}
		return null;
	}

}
