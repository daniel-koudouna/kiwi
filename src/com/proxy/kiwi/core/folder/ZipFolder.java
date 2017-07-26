package com.proxy.kiwi.core.folder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

public class ZipFolder extends Folder{

	File extractDirectory;

	public ZipFolder(File file, String name, Folder parent, File initial) {
		super(file, name, parent, initial);
	}

	@Override
	protected void loadChildren() {

		List<Folder> children = new ArrayList<>();
		
		try {
			extractDirectory = Files.createTempDirectory(Folders.getTempPath(), file.getName()).toFile();

			ZipFile zipFile = new ZipFile(file);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(extractDirectory, entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
					children.add(new FileFolder(entryDestination, entry.getName(), this, initial));
				}
			}

			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!children.isEmpty()) {
			this.children = Optional.of(children);			
		} else {
			this.children = Optional.empty();
		}

	}

	@Override
	protected void loadImagesImpl(boolean partial) {

		List<FolderImage> images = new ArrayList<>();
		
		try {
			ZipFile zipFile = new ZipFile(file);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(extractDirectory, entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else if (ItemType.get(entry) == ItemType.IMAGE){
					entryDestination.getParentFile().mkdirs();

					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();
					
					if (entryDestination.getParentFile().getAbsolutePath().equals(extractDirectory)) {
						images.add(new FolderImage(entryDestination, entryDestination.getName(), this));
						if (partial) {
							break;
						}
					}
				}
			}
			
			zipFile.close();
		} catch (IOException e) {

		}
		
		if (images.isEmpty()) {
			this.images = Optional.empty();
		} else {
			this.images = Optional.of(images);
		}
		
		children.ifPresent(list -> list.forEach(f -> f.loadImages(false)));
	}

}
