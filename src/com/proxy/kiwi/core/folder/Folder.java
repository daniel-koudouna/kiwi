package com.proxy.kiwi.core.folder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import com.proxy.kiwi.core.service.GenericTaskService;
import com.proxy.kiwi.core.utils.Dynamic;

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
public abstract class Folder extends Item{
	Optional<List<Folder>> children;
	Optional<List<FolderImage>> images;

	Dynamic<Boolean> hasLoaded;
	Dynamic<Boolean> hasLoadedPartially;
	
	int startPage = 1;
	protected File initial;
	
	/**
	 * Folders use a two-pass system to improve speed. A folder does enough work only to create the other nodes in the tree, but defers
	 * loading of the images, which are usually much more numerous than the folders, to the {@link GenericTaskService}.
	 * 
	 * @param file The file which contains the children
	 * @param name The internal name of the folder
	 * @param parent The parent of the folder, or null if the folder is a root node
	 * @param autoLoad Whether or not to automatically queue image loading
	 */
	public Folder(File file, String name, Folder parent, File initial, boolean autoLoad) {
		super(file,name,parent);
		hasLoaded = new Dynamic<Boolean>(false);
		hasLoadedPartially = new Dynamic<Boolean>(false);
		children = Optional.empty();
		images = Optional.empty();
		this.parent = Optional.ofNullable(parent);
		this.initial = initial;

		loadChildren();
		
		if (autoLoad) {
			GenericTaskService.enqueue(()-> this.loadImages(false));
		}
		
		GenericTaskService.enqueue(()-> this.loadImages(true));
	}

	public Folder(File file, String name, Folder parent, File initial) {
		this(file,name,parent,initial,false);
	}
	
	public long childrenCount() {
		return children().count();
	}

	public long imageCount() {
		return images.orElse(new LinkedList<>()).size();
	}

	public Stream<Folder> children() {
		if (children.isPresent()) {
			return children.get().stream();
		} else {
			return Stream.empty();
		}
	}

	public Stream<FolderImage> images() {
		if (images.isPresent()) {
			return images.get().stream();
		} else {
			return Stream.empty();
		}
	}

	public Stream<Item> stream() {
		return Stream.concat(children(), images());
	}

	public List<Folder> getChildren() {
		return children.orElse(new LinkedList<>());
	}
	
	public List<FolderImage> getImages() {
		return images.orElse(new LinkedList<>());
	}

	/**
	 * For any children with single children, collect them
	 * as children of the original folder.
	 */
	public void refactor() {
		if (!children.isPresent()) {
			return;
		}
		for (Folder child : children.get()) {
			child.refactor();					
		}
		if (hasLoaded.get() == false) {
			return;
		}
		for (int i = 0; i < children.get().size(); i++) {
			Folder child = children.get().get(i);
			if (!child.children.isPresent() && !child.images.isPresent() && child.hasLoaded.get()) {
				children.get().remove(i);
				i--;
			} else if (child.children.isPresent() && child.children.get().size() == 1 && child.hasLoaded.get()) {
				Folder f2 = child.children.get().get(0);
				children.get().remove(i);
				children.get().add(i, f2);
				f2.setParent(this);
				f2.setName(child.getName() + " - " + f2.getName());
				i--;
			}
		}
	}

	public boolean hasAncestor(Folder parent) {
		return (this.parent.isPresent() && (this.parent.get().equals(parent) || this.parent.get().hasAncestor(parent)));
	}

	public boolean hasAncestor(String name) {
		return (this.parent.isPresent() && (this.parent.get().getName().toLowerCase().contains(name) || this.parent.get().hasAncestor(name)));
	}

	public static Optional<Folder> fromFile(String path) {
		return fromFile(path, true);
	}
	
	public static Optional<Folder> fromFile(File file, boolean retry) {
		return fromFile(file.getAbsolutePath(), retry);
	}
	
	public static Optional<Folder> fromFile(String path, boolean retry) {
		Optional<Folder> folder = fromFile(new File(path), new File(path));
		if (!folder.isPresent() && retry) {
			folder = fromFileBackup(path);
		}
		
		return folder;
	}
	
	public static Optional<Folder> fromFile(File file, File initial) {
		Optional<Item> item = getFrom(file,null, initial);
		if (item.isPresent() && item.get() instanceof Folder) {
			return Optional.of((Folder) item.get());
		} else if (item.isPresent()) {
			return fromFile(file.getParentFile(), initial);
		} else {
			return Optional.empty();
		}
	}

	public int findPartial(String filename) {
		if (!images.isPresent()) {
			return 1;
		}
		
		for (int i = 0; i < images.get().size(); i++) {
			FolderImage img = images.get().get(i);
			if (img.getFile().getName().equals(filename)) {
				return i + 1;
			}
		}
		
		return 1;
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

	public Optional<Folder> next() {
		Optional<Folder> parent = Folder.fromFile(file.getParentFile(), true);
		if (!parent.isPresent()) {
			return Optional.empty();
		}
		
		List<Folder> subFolders = parent.get().children.orElse(Collections.emptyList());
		
		boolean found = false;
		for (Folder child : subFolders) {
			if (found) {
				return Optional.of(child);
			}

			if (child.getFile().getAbsolutePath().equals(this.getFile().getAbsolutePath())) {
				found = true;
			}
		}
		
		return Optional.empty();
	}
	
	public Optional<Folder> previous() {
		Optional<Folder> parent = Folder.fromFile(file.getParentFile(), true);
		if (!parent.isPresent()) {
			return Optional.empty();
		}
		
		List<Folder> subFolders = parent.get().children.orElse(Collections.emptyList());
		
		Folder prev = null;
		for (Folder child : subFolders) {
			if (child.getFile().getAbsolutePath().equals(this.getFile().getAbsolutePath())) {
				if (prev != null) {
					return Optional.of(prev);
				}
			}
			prev = child;
		}
		
		return Optional.empty();
	}
	
	public Dynamic<Boolean> getLoaded() {
		return hasLoaded;
	}

	public boolean contains(File file) {
		return images.orElse(Collections.emptyList()).stream().anyMatch(image -> image.file.getAbsolutePath().equals(file.getAbsolutePath()));
	}
	
	protected abstract void loadChildren();
	
	/**
	 * The single method exposed for loading images, either partially or fully.
	 */
	public void loadImages(boolean partial) {
		if (!partial && hasLoaded.get()) {
			return;
		}
		if (partial && (hasLoadedPartially.get() || hasLoaded.get())) {
			return;
		}
		loadImagesImpl(partial);
		if (partial) {
			this.hasLoadedPartially.set(true);
		} else {
			this.hasLoaded.set(true);
		}
	}

	/** The default image loading is not partial. */
	public void loadImages() {
		loadImages(false);
	}
	
	/** 
	 * The internal image loading, implemented separately to 
	 * allow for different folder abstractions.
	 */
	protected abstract void loadImagesImpl(boolean partial);
	
	protected Optional<Item> getItem(File file) {
		return getFrom(file,this, initial);
	}

	private static Optional<Item> getFrom(File file, Folder parent, File initial) {
		return Item.from(file, parent, initial);
	}

	/** 
	 * Backup strategy to load a file when primary folder loading fails.
	 * Fixes Windows incompatibility with UTF-8 symbols in folder names.
	 */
	private static Optional<Folder> fromFileBackup(String input) {
		File path = new File(input);	

		Stack<String> stack = new Stack<>();

		while (!path.isDirectory()) {
			stack.push(path.getName().toString());
			path = path.getParentFile();
		}

		File cd = path;

		boolean failed = false;
		while (!stack.isEmpty() && !failed) {
			String file = stack.pop();
			
			List<File> files = Arrays.asList(cd.listFiles());

			Optional<File> next = files.stream()
					.filter(f -> {
						
						byte[] bytes = file.getBytes(Charset.defaultCharset());
						String n = new String(bytes, StandardCharsets.US_ASCII);

						String s1 = n;
						String s2 = f.getName();

						return Arrays.equals(s1.getBytes(), s2.getBytes());
					})
					.findFirst();

			if (next.isPresent()) {
				cd = next.get();
			} else {
				failed = true;
			}
		}

		if (failed) {
			return Optional.empty();
		} else {
			System.out.println(cd.getAbsolutePath());
			return Folder.fromFile(cd, false);
		}
		
	}

	public int getStartPage() {
		loadImages(false);
		if (!initial.isDirectory()) {
			return find(initial.getAbsolutePath());
		} else {
			return 1;
		}
	}

	public Optional<FolderImage> getFirstImage() {
		loadImages(true);
		FolderImage image = findFirstImage();
		if (image == null) {
			return Optional.empty();
		} else {
			return Optional.of(image);
		}
	}

	private FolderImage findFirstImage() {
		if (images.isPresent() && images.get().size() > 0) {
			return images.get().get(0);
		} else if (children.isPresent() && children.get().size() > 0) {
			return children.get().get(0).findFirstImage();
		} else {
			return null;
		}
	}
	
	public boolean hasSubfolders() {
		return children.isPresent() && !children.get().isEmpty();
	}
}
