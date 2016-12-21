package com.proxy.kiwi.core.services;

import com.proxy.kiwi.core.folder.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Folders {
	private static Folder root = null;

	private static String currentFolder = "";

	private static Path tempPath;

	static {
		tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "Kiwi");
		tempPath.toFile().mkdirs();
	}

	public static Thread thread;

	public static Folder getRoot() {
		return root;
	}

	public static String getCurrentFolderBuilt() {
		return currentFolder;
	}

	public static Path getTempPath() {
		return tempPath;
	}

	public static void buildDefaultRoot() {
		thread = new Thread(() -> {
			buildRoot(Config.getOption("path"));
		});

		thread.start();
	}

	public static Folder buildOnlyFromFile(String filepath) {
		File file = new File(filepath);
		switch (Type.getType(file)) {
		case IMAGE:
			file = file.getParentFile();
			return new FileFolder(file.getName(), file.getAbsolutePath(), root);
		case OTHER:
			break;
		case RAR:
			return new RarFolder(file.getName(), file.getAbsolutePath(), root);
		case SZ:
			break;
		case TAR:
			break;
		case ZIP:
			return new ZipFolder(file.getName(), file.getAbsolutePath(), root);
		case FOLDER:
			return new FileFolder(file.getName(), file.getAbsolutePath(), root);
		default:
			break;

		}
		return null;
	}

	public static void buildRoot(String rootpath) {
		File rootFile = new File(rootpath);

		root = new FileFolder(rootFile.getName(), rootpath, null);
		clean(root);
		request(root);
	}

	public String getImagePath(Folder folder) {
		if (Config.getFolderImage(folder.getName()) != null) {
			return Config.getFolderImage(folder.getName());
		} else {
			return folder.getImagePath();
		}
	}

	public static void print() {
		print(root, 0);
	}

	private static void print(Folder folder, int depth) {
		for (Folder child : folder.getSubfolders()) {
			print(child, depth + 1);
		}
	}

	public static void loadFolder(Folder folder) {
		folder.load();
	}

	private static void request(Folder folder) {
		for (Folder child : folder.getSubfolders()) {
			Thumbnails.request(child);
		}
		for (Folder child : folder.getSubfolders()) {
			request(child);
		}
	}

	/**
	 * Removes any empty folders from the tree. Flattens folders with only one
	 * subfolder.
	 */
	public static void clean(Folder folder) {
		if (folder.hasSubfolders()) {
			for (Folder child : folder.getSubfolders()) {
				clean(child);
			}
			for (int i = 0; i < folder.getSubfolders().size(); i++) {
				Folder child = folder.getSubfolders().get(i);
				if (!child.hasSubfolders() && !child.hasVolumes()) {
					folder.getSubfolders().remove(i);
				} else if (child.getSubfolders().size() == 1) {
					Folder f2 = child.getSubfolders().get(0);
					folder.getSubfolders().remove(i);
					folder.getSubfolders().add(i, f2);
					f2.setParent(folder);
					f2.getNameProperty().set(child.getName() + " - " + f2.getName());
				}
			}

		}
	}

	public static void addToSize(Folder folder) {

	}

	/**
	 * Deletes the temporary directory made when extracting archives.
	 */
	public static void deleteTemps() {

		try {
			Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					if (e == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});
		} catch (FileSystemException e) {

		} catch (IOException e) {
			// TODO error message?
			e.printStackTrace();
		}

	}

	public static void uproot() {
		Folder temp = root;
		root = new FileFolder("root", "", null);
		root.addFolder(temp);
	}

	public static int find(Folder folder, String filename) {
		if (!folder.getIsLoadedProperty().get()) {
			loadFolder(folder);
		}

		for (int i = 0; i < folder.getVolumes().size(); i++) {
			Volume v = folder.getVolumes().get(i);
			if (v.getFilename().equals(filename)) {
				return i + 1;
			}
		}

		return 1;
	}
}
