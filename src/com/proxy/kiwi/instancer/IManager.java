package com.proxy.kiwi.instancer;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class IManager<T extends Serializable> {

    /***
     * Sleeps, waiting for an object of type T to be written
     * to an appropriate data file.
     * @see IManager#wake(Serializable)
     */
	@SuppressWarnings("unchecked")
	public void sleep() {
        int i = findFreeIndex();
        if (i == -1) {
            return;
        }

        new Thread( () -> {
            try {
                getTempPath().toFile().mkdirs();

                File lock = getLockFile(i);

                RandomAccessFile lockedFile = new RandomAccessFile(lock, "rw");
                FileLock fileLock = lockedFile.getChannel().tryLock();

                pause();

                File file = getDataFile(i);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);

                T input = null;
                try {
                    input = (T) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                file.delete();

                fileLock.release();
                lockedFile.close();

                ois.close();

                resume(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /***
     * Attempts to wake a sleeping instance by communicating a serializable
     * object over a file.
     * @param input The input to be passed to the sleeping instance.
     * @return <b>true</b> if an instance was woken, <b>false</b> otherwise.
     */
    public boolean wake(T input) {
        try {
            int i = findLockIndex();
            if (i != -1) {
                File file = getDataFile(i);

                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(input);
                oos.close();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /***
     * Implementation defined method to pause the current execution.
     */
    public abstract void pause();

    /***
     * Implementation defined method to resume the exeuction given
     * a new input.
     * @param input The input given to the sleeping instance.
     */
    public abstract void resume(T input);

    /***
     * Method called when shutting down the JVM.
     */
    public abstract void shutdown();

    /***
     * @return The name of the files used for locking.
     */
    protected abstract String getLockName();

    /***
     * @return The name of the files containing the transferred data.
     */
    protected abstract String getDataName();

    /***
     * @return The path to write temporary files into.
     */
    protected abstract Path getTempPath();

    /***
     * @return The file name for the data file at index i.
     */
    private File getDataFile(int i) {
        return Paths.get(getTempPath().toString(), getDataName() + "-" + (i < 10 ? "0" + i : i)).toFile();
    }

    /***
     * @return The file name for lock at index i.
     */
    private File getLockFile(int i) {
        return Paths.get(getTempPath().toString(), getLockName() + "-" + (i < 10 ? "0" + i : i)).toFile();
    }

    /***
     * Find the first available free index for locking.
     * @return i >= 0 if an index is found, -1 otherwise.
     */
    private int findFreeIndex() {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

    }

    /***
     * Find the first available locked index for unlocking.
     * @return i >= 0 if an index is found, -1 otherwise.
     */
    private int findLockIndex() {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
