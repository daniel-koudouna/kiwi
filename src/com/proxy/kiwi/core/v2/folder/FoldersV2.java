package com.proxy.kiwi.core.v2.folder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FoldersV2 {

	private static Path tempPath;
	
	static {
		tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "Kiwi");
		tempPath.toFile().mkdirs();
	}

	public static Path getTempPath() {
		return tempPath;
	}
	
	private static List<FolderV2> listOf(FolderV2 source) {
		List<FolderV2> list = new LinkedList<>();
		list.add(source);
		source.folderStream().forEach(child -> {
			list.addAll(listOf(child));
		});
		return list;
	}
	
	public static Stream<FolderV2> DFS(FolderV2 source) {
		return listOf(source).stream();
	}
	
	public FolderV2 rootFrom(String rootPath, String...paths) {
		FolderV2 root = new FileFolderV2(new File(rootPath), "root", null, new File(rootPath));
		
		for (String path: paths) {
			FolderV2 folder = new FileFolderV2(new File(path), "xroot", null, new File(path));
			folder.folderStream().forEach(f -> {
				f.parent = Optional.of(root);
				root.children.get().add(f);
			});
		}
		
		return root;
	}

}
