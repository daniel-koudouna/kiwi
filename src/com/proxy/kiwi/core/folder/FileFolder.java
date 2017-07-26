package com.proxy.kiwi.core.folder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileFolder extends Folder{

	public FileFolder(File file, String name, Folder parent, File initial) {
		super(file, name, parent, initial);
	}

	@Override
	protected void loadChildren() {
		File[] entries = file.listFiles();
		if (entries == null || entries.length == 0) {
			return;
		}
		
		List<Folder> items = new ArrayList<>(entries.length);

		
		for (File entry : entries) {
			ItemType type = ItemType.get(entry);
			if (type == ItemType.IMAGE) {
				break;
			}
			
			getItem(entry).ifPresent(item  -> {
				if (item instanceof Folder) {
					items.add((Folder) item);
				}
			});
		}
		
		this.children = Optional.of(items);
	}

	@Override
	protected void loadImagesImpl(boolean partial) {
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
			
			Item image = getItem(entry).get();
			if (image != null && image instanceof FolderImage) {
				items.add((FolderImage) image);
				if (partial) {
					break;					
				}
			}
		}
		
		this.images = Optional.of(items);
	}

}
