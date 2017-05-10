package com.proxy.kiwi.core.v2.folder;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.folder.Volume;
import com.proxy.kiwi.core.utils.Dynamic;
import com.proxy.kiwi.core.v2.service.GenericTaskService;

/**
 * A Tree-like data structure which acts as an abstraction for the files and images stored in various
 * parts of the system. It contains other Folders as child nodes,
 * and FolderImages as leaves.
 * <br/><br/>
 * The advantages of an additional abstraction are many-fold:
 * <ul>
 * <li>The files need not be stored in a traditional top-down structure. This allows the merging of multiple tree roots to create
 * a bigger collection.</li>
 * <li>The files need not be stored in folders recognized by the operating system. Other storage formats are made
 * possible, such as archives, which need to unzip their contents into temporary folders before displaying them.</li>
 * </ul>
 * 
 * @author Daniel
 *
 */
public abstract class FolderV2 extends Item{
	Optional<List<FolderV2>> children;
	Optional<List<FolderImage>> images;

	Dynamic<Boolean> hasLoaded;

	/**
	 * Folders use a two-pass system to improve speed. A folder does enough work only to create the other nodes in the tree, but defers
	 * loading of the images, which are usually much more numerous than the folders, to the {@link GenericTaskService}.
	 * 
	 * @param file The file which contains the children
	 * @param name The internal name of the folder
	 * @param parent The parent of the folder, or null if the folder is a root node
	 */
	public FolderV2(File file, String name, FolderV2 parent) {
		super(file,name,parent);
		hasLoaded = new Dynamic<Boolean>(false);
		children = Optional.empty();
		images = Optional.empty();
		this.parent = Optional.ofNullable(parent);

		loadChildren();

		GenericTaskService.enqueue(this::loadImagesInternal);
	}

	public long folderSize() {
		return folderStream().count();
	}
	
	public long imageSize() {
		return imageStream().count();
	}
	
	public Stream<FolderV2> folderStream() {
		if (children.isPresent()) {
			return children.get().stream();
		} else {
			return Stream.empty();
		}
	}

	public Stream<FolderImage> imageStream() {
		if (images.isPresent()) {
			return images.get().stream();
		} else {
			return Stream.empty();
		}
	}

	public Stream<Item> stream() {
		return Stream.concat(folderStream(), imageStream());
	}


	public void refactor() {
		//pull children up
		if (!children.isPresent()) {
			return;
		}
		for (FolderV2 child : children.get()) {
			child.refactor();					
		}
		if (hasLoaded.get() == false) {
			return;
		}
		for (int i = 0; i < children.get().size(); i++) {
			FolderV2 child = children.get().get(i);
			if (!child.children.isPresent() && !child.images.isPresent() && child.hasLoaded.get()) {
				children.get().remove(i);
				i--;
			} else if (child.children.isPresent() && child.children.get().size() == 1 && child.hasLoaded.get()) {
				FolderV2 f2 = child.children.get().get(0);
				children.get().remove(i);
				children.get().add(i, f2);
				f2.setParent(this);
				f2.setName(child.getName() + " - " + f2.getName());
				i--;
			}
		}
	}

	public boolean hasAncestor(FolderV2 parent) {
		return (this.parent.isPresent() && (this.parent.get().equals(parent) || this.parent.get().hasAncestor(parent)));
	}
	
	public boolean hasAncestor(String name) {
		return (this.parent.isPresent() && (this.parent.get().getName().toLowerCase().contains(name) || this.parent.get().hasAncestor(name)));
	}
	
	public int find(String absolutePath) {

		if (!images.isPresent()) {
			return 1;
		}
		
		for (int i = 0; i < images.get().size(); i++) {
			FolderImage img = images.get().get(i);
			if (img.getFile().getAbsolutePath().equals(absolutePath)) {
				return i + 1;
			}
		}

		return 1;
	}
	
	private void loadImagesInternal() {
		this.loadImages();
		this.hasLoaded.set(true);
	}
	
	protected abstract void loadChildren();

	protected abstract void loadImages();

	protected Optional<Item> getItem(File file) {
		return Item.from(file, this);
	}
}
