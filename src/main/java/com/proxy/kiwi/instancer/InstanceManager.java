package com.proxy.kiwi.instancer;

import com.proxy.kiwi.tree.Tree;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class InstanceManager extends IManager<Tree> {

    private List<File> tempFilesCreated;

    public InstanceManager() {
        tempFilesCreated = new LinkedList<>();
    }

    public void registerTempFile(File file) {
        tempFilesCreated.add(file);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume(Tree input) {

    }

    @Override
    public void shutdown() {
        tempFilesCreated.stream().forEach(f -> {
            deleteDir(f);
        });
    }

    @Override
    protected String getLockName() {
        return "kiwi.lock";
    }

    @Override
    protected String getDataName() {
        return "kiwi.data";
    }

    @Override
    protected Path getTempPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "Kiwi");
    }

    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}
