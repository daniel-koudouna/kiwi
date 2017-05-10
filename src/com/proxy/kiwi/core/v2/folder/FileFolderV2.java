package com.proxy.kiwi.core.v2.folder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileFolderV2 extends FolderV2{

	public FileFolderV2(File file, String name, FolderV2 parent) {
		super(file, name, parent);
	}

	@Override
	protected void loadChildren() {
		File[] entries = file.listFiles();
		if (entries == null || entries.length == 0) {
			return;
		}
		
		List<FolderV2> items = new ArrayList<>(entries.length);

		
		for (File entry : entries) {
			ItemType type = ItemType.get(entry);
			if (type == ItemType.IMAGE) {
				break;
			}
			
			getItem(entry).ifPresent(item  -> {
				if (item instanceof FolderV2) {
					items.add((FolderV2) item);
				}
			});
		}
		
		this.children = Optional.of(items);
	}

	@Override
	protected void loadImages() {
		File[] entries = file.listFiles();
		if (entries == null || entries.length == 0) {
			return;
		}
		
		List<FolderImage> items = new ArrayList<>(entries.length);

		
		for (File entry : entries) {
			ItemType type = ItemType.get(entry);
			if (type != ItemType.IMAGE) {
				continue;
			}
			
			getItem(entry).ifPresent(item -> {
				if (item instanceof FolderImage) {
					items.add((FolderImage) item);
				}
			});
		}
		
		this.images = Optional.of(items);
	}


}
