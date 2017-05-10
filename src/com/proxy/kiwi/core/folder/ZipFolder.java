package com.proxy.kiwi.core.folder;

import org.apache.commons.io.IOUtils;

import com.proxy.kiwi.core.services.Folders;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFolder extends Folder {

	public File outputDir;
	HashMap<Folder, Boolean> loaded;

	public ZipFolder(String name, String path, Folder parent) {
		super(name, path, parent);
		loaded = new HashMap<>();
	}

	@Override
	public void load() {
		load(this);
	}

	public void load(Folder folder) {
		if (loaded.get(folder) != null && loaded.get(folder).equals(Boolean.TRUE)) {
			return;
		}
		loaded.put(folder, Boolean.TRUE);
		new Thread(new UnzipRunner(this, folder)).start();
	}

	@Override
	protected File[] build() {
		try {
			File file = new File(filename.get());
			outputDir = Files.createTempDirectory(Folders.getTempPath(), file.getName()).toFile();

			ZipFile zipFile = null;
			zipFile = new ZipFile(file);

			boolean hasLoaded = false;

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements() && !hasLoaded) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(outputDir, entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else if (Type.getType(entry) == Type.IMAGE){
					entryDestination.getParentFile().mkdirs();

					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();

					addVolume(new Volume(entryDestination.getName(), entryDestination.getAbsolutePath(), this));

				}
			}
			hasLoaded = true;
			zipFile.close();
		} catch (IOException e) {

		}
		return outputDir.listFiles();

	}

	@Override
	protected String filterName(String name) {
		return name;
	}

}

class UnzipRunner implements Runnable {

	ZipFolder folder;
	Folder target;

	UnzipRunner(ZipFolder folder, Folder target) {
		this.folder = folder;
		this.target = target;
		System.out.println("STARTING");
	}

	@Override
	public void run() {

		try {
			target.clearVolumes();
			File file = new File(folder.filename.get());

			ZipFile zipFile = null;
			zipFile = new ZipFile(file);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(folder.outputDir, entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();

					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();

					target.addVolume(
							new Volume(entryDestination.getName(), entryDestination.getAbsolutePath(), target));

				}
			}
			System.out.println("DONE");
			zipFile.close();
		} catch (IOException e) {

		}

		Collections.sort(target.getVolumes());

	}
}
