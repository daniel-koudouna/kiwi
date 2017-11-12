package com.proxy.kiwi.core.folder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Folders {

	private static Path tempPath;
	private static List<File> tempFolders;

	static {
		tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "Kiwi");
		tempPath.toFile().mkdirs();
		tempFolders = new LinkedList<>();
	}

	public static Path getTempPath() {
		return tempPath;
	}

	private static List<Folder> listOf(Folder source) {
		List<Folder> list = new LinkedList<>();
		list.add(source);
		source.children().forEach(child -> {
			list.addAll(listOf(child));
		});
		return list;
	}

	public static Stream<Folder> DFS(Folder source) {
		return listOf(source).stream();
	}

	public static Folder rootFrom(String rootPath, String...paths) {
		Folder root = new FileFolder(new File(rootPath), "root", null, new File(rootPath));

		for (String path: paths) {
			Folder folder = new FileFolder(new File(path), "xroot", null, new File(path));
			folder.children().forEach(f -> {
				f.parent = Optional.of(root);
				root.children.get().add(f);
			});
		}

		root.refactor();
		return root;
	}

	public static Folder rootFrom(List<String> paths) {
		String head = paths.remove(0);
		return rootFrom(head, paths.toArray(new String[0]));
	}

	public static void addTempFolder(File file) {
		tempFolders.add(file);
	}

	public static List<File> getTempFiles() {
		return tempFolders;
	}

}
