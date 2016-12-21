package com.proxy.kiwi.core.image;

import java.io.File;
import java.io.IOException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import javafx.scene.image.Image;

public class KiwiImage extends Image {

	private Orientation orientation;
	private Metadata metadata;

	public KiwiImage(File file) {
		this(file, 0, 0, false, true, false);
	}

	public KiwiImage(File file, double width, double height, boolean preserveRatio, boolean smooth,
			boolean backgroundLoading) {
		super(file.toURI().toString(), width, height, preserveRatio, smooth, backgroundLoading);
		this.metadata = getMetadata(file);
		this.orientation = setOrientation();
	}

	public Orientation getOrientation() {
		return orientation;
	}

	private Orientation setOrientation() {
		if (metadata == null) {
			return Orientation.NONE;
		}

		for (Directory dir : metadata.getDirectories()) {
			for (Tag tag : dir.getTags()) {
				if (tag.getTagName().equals("Orientation")) {
					String description = tag.getDescription();
					if (description.contains("Rotate 90 CW")) {
						return Orientation.CW_HALF;
					}
					// TODO find other rotation descriptions
					break;
				}
			}
		}

		return Orientation.NONE;
	}

	private Metadata getMetadata(File file) {
		try {
			return ImageMetadataReader.readMetadata(file);
		} catch (ImageProcessingException | IOException e) {
			return null;
		}
	}

}
