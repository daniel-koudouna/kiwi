package com.proxy.kiwi.core.folder;

import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.util.zip.ZipEntry;

public enum Type {
	FOLDER, IMAGE, RAR, ZIP, SZ, TAR, OTHER;

	public static Type getType(FileHeader header) {
		if (header.isDirectory()) {
			return FOLDER;
		} else {
			return getType(header.getFileNameString());
		}
	}

	public static Type getType(ZipEntry entry) {
		if (entry.isDirectory()) {
			return FOLDER;
		} else {
			return getType(entry.getName());
		}
	}

	public static Type getType(File file) {
		if (file.isDirectory()) {
			return FOLDER;
		} else {
			return getType(file.getAbsolutePath());
		}
	}

	public static Type getType(String fn) {
		fn = fn.toLowerCase();
		if (fn.endsWith(".jpg") || fn.endsWith(".png") || fn.endsWith(".gif")) {
			return IMAGE;
		}
		if (fn.endsWith(".rar")) {
			return RAR;
		}
		if (fn.endsWith(".zip") || fn.endsWith(".7z") || fn.endsWith(".tar") || fn.endsWith(".tar.gz")) {
			return ZIP;
		}
		return OTHER;
	}
}
