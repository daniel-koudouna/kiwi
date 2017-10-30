package com.proxy.kiwi.core.folder;

import java.io.File;
import java.util.zip.ZipEntry;

import com.github.junrar.rarfile.FileHeader;

public enum ItemType {
	FOLDER,
	IMAGE,
	ZIP,
	SZ,
	TAR,
	UNKNOWN;

	public static ItemType get(FileHeader header) {
		if (header.isDirectory()) {
			return FOLDER;
		} else {
			return get(header.getFileNameString());
		}
	}

	public static ItemType get(ZipEntry entry) {
		if (entry.isDirectory()) {
			return FOLDER;
		} else {
			return get(entry.getName());
		}
	}

	public static ItemType get(File file) {
		if (file.isDirectory()) {
			return FOLDER;
		} else {
			return get(file.getAbsolutePath());
		}
	}

	private static ItemType get(String name) {
		String filename = name.toLowerCase();
		if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".gif")) {
			return IMAGE;
		} else if (filename.endsWith(".zip") || filename.endsWith(".7z") || filename.endsWith(".tar") || filename.endsWith(".tar.gz") || filename.endsWith(".cbz")) {
			return ZIP;
		}

		return UNKNOWN;
	}
}
