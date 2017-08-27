package com.proxy.kiwi.core.services;

import com.proxy.kiwi.core.utils.Stopwatch;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Instancer {

	/*
	 * search all files if there is a locked file (locked => program still open)
	 * go to the corresponding data file ( since program is waiting for input)
	 * write path exit else start normally
	 * 
	 * 
	 */

	private static Path tempPath;

	static {
		tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "Kiwi");
		tempPath.toFile().mkdirs();
	}
	
	public static final String SELF_WAKE = "-1";

	public abstract void pause(FileLock lock);

	public abstract void resume(String input);

	public abstract void shutdown();

	protected abstract String getLockName();

	protected abstract String getDataName();

	public boolean sleep() throws IOException, OverlappingFileLockException {
		int i = findFree();
		if (i != -1) {

			new Thread(() -> {
				try {
					File lock = getLockFile(i);

					RandomAccessFile lockedFile = new RandomAccessFile(lock, "rw");
					FileLock fileLock = lockedFile.getChannel().tryLock();

					pause(fileLock);

					File file = getDataFile(i);
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String input = null;
					do {
						Thread.sleep(50);
						input = reader.readLine();
					} while (input == null);

					file.delete();

					fileLock.release();
					lockedFile.close();
					reader.close();
					resume(input);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();

			return true;
		} else {
			return false;
		}
	}

	public boolean wakeIfExists(String[] args) throws IOException {
		Stopwatch.click("Checking for sleeping instances");
		int i = findLock();
		if (i != -1) {
			File file = getDataFile(i);
			
			try {
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
				
				writer.write(args.length == 0 ? " " : args[0]);
				writer.close();
				Stopwatch.click("Checking for sleeping instances");
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		Stopwatch.click("Checking for sleeping instances");
		return false;
	}

	public int findFree() throws IOException, OverlappingFileLockException {
		for (int i = 0; i < 10; i++) {
			File file = getLockFile(i);
			RandomAccessFile lockedFile = new RandomAccessFile(file, "rw");
			FileLock lock = lockedFile.getChannel().tryLock();
			if (lock != null) {
				lock.release();
				lockedFile.close();
				return i;
			}
			lockedFile.close();
		}
		return -1;
	}

	public int findLock() throws IOException {
		for (int i = 0; i < 10; i++) {
			File file = getLockFile(i);
			RandomAccessFile lockedFile = new RandomAccessFile(file, "rw");
			FileLock lock = lockedFile.getChannel().tryLock();
			if (lock == null) {
				lockedFile.close();
				return i;
			} else {
				lock.release();
			}
			lockedFile.close();
		}

		return -1;
	}

	private File getDataFile(int i) {
		return Paths.get(tempPath.toString(), getDataName() + "-" + (i < 10 ? "0" + i : i)).toFile();
	}

	private File getLockFile(int i) {
		return Paths.get(tempPath.toString(), getLockName() + "-" + (i < 10 ? "0" + i : i)).toFile();
	}
}
