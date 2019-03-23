package com.proxy.kiwi.image;

import java.io.File;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;

public class KMetadata {
    Metadata rawMetadata;
    public int width;
    public int height;

    public KMetadata(String path) {
        try {
            this.rawMetadata = ImageMetadataReader.readMetadata(new File(path));
            readDimensions();
        } catch (Exception e) {
        	System.err.println("Error reading metadata for " + path);
            width = 0;
            height = 0;
        }
    }

    private void readDimensions() {
        for (Directory d : rawMetadata.getDirectories()) {
            for (Tag t : d.getTags()) {
                String name = t.getTagName();
                if (name.equals("Image Width")) {
                    width = readPixels(t.getDescription());
                } else if (name.equals("Image Height")) {
                    height = readPixels(t.getDescription());
                }
            }
        }
    }

    private int readPixels(String name) {
        try {
            String s = name.replaceAll("pixels", "").trim();
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
