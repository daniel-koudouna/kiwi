package com.proxy.kiwi.core.folder;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.proxy.kiwi.core.services.Folders;
import com.proxy.kiwi.core.utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;

/**
 * Folder created with the contents of a .rar file. The .rar file requires an
 * external library to extract, as it is a proprietary format.
 * 
 * @author Daniel
 *
 */
// TODO make new class archive folder with refactored loading
public class RarFolder extends Folder {

	public File outputDir;
	/*
	 * HashMap containing all the children's loaded status. Independent of the
	 * Folder's loaded property, since they are loaded through the parent.
	 */
	HashMap<Folder, Boolean> loaded;

	public RarFolder(String name, String path, Folder parent) {
		super(name, path, parent);
		loaded = new HashMap<>();
	}

	@Override
	public void load() {
		load(this);
	}

	/**
	 * Load a folder inside the rar file, which could be the rar file itself.
	 * 
	 * @param folder
	 *            the folder in question
	 */
	public void load(Folder folder) {
		if (loaded.get(folder) != null && loaded.get(folder).equals(Boolean.TRUE)) {
			return;
		}
		loaded.put(folder, Boolean.TRUE);
		new Thread(new UnrarRunner(this, folder)).start();
	}

	@Override
	protected File[] build() {
		File file = new File(filename.get());
		try {
			outputDir = Files.createTempDirectory(Folders.getTempPath(), file.getName()).toFile();
		} catch (IOException e1) {
			// TODO error message? probably privilages
			e1.printStackTrace();
		}

		try {
			Archive a = new Archive(file);

			for (FileHeader header : a.getFileHeaders()) {
				File entryDestination = new File(outputDir, header.getFileNameString());
				if (header.isDirectory()) {
					entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();
					int imgs = 0;
					if (entryDestination.getParentFile() != null) {
						for (File f : entryDestination.getParentFile().listFiles()) {
							if (!f.isDirectory()) {
								imgs++;
							}
						}
					}

					if (imgs < 2 && Type.getType(header) == Type.IMAGE) {
						OutputStream out = new FileOutputStream(entryDestination);
						a.extractFile(header, out);
						out.close();
					}

				}
			}
			a.close();
		} catch (RarException | IOException e) {
			// TODO error message or something
			e.printStackTrace();
		}
		return outputDir.listFiles();
	}

	@Override
	protected String filterName(String name) {
		return name.replaceAll("(\\(.*?\\))|(\\[.*?\\])|(\\{.*?\\})|(=.*?=)|(~.*?~)", "").replaceAll("\\A\\s*", "")
				.replaceAll(".rar", "").trim();
	}

	protected static boolean zipFileIsImage(String name) {
		return name.contains(".jpg") || name.contains(".gif") || name.contains(".png");
	}
}

class UnrarRunner implements Runnable {

	RarFolder folder;
	Folder target;

	public UnrarRunner(RarFolder folder, Folder target) {
		this.folder = folder;
		this.target = target;
	}

	@Override
	public void run() {
		target.clearVolumes();

		int fileCount = 0;

		try {
			File file = new File(folder.getFilenameProperty().get());
			Archive a = new Archive(file);
			Log.print(Log.IO, "Loading folder '" + target.getName() + "' from .rar file '" + folder.getName() + "'");
			for (FileHeader header : a.getFileHeaders()) {
				File entryDestination = new File(folder.outputDir, header.getFileNameString());
				if (header.isDirectory()) {
					entryDestination.mkdirs();
				} else if (Type.getType(header) == Type.IMAGE) {
					entryDestination.getParentFile().mkdirs();

					if (target.shouldContain(entryDestination)) {

						OutputStream out = new FileOutputStream(entryDestination);
						a.extractFile(header, out);
						out.close();
						fileCount++;
						target.addVolume(
								new Volume(entryDestination.getName(), entryDestination.getAbsolutePath(), target));
					}

				}
			}
			a.close();
			Log.print(Log.IO, "Extracted " + fileCount + " files for " + target.getName());
		} catch (RarException | IOException e) {
			// TODO error message
			e.printStackTrace();
		}

		Collections.sort(target.getVolumes());
	}

}
