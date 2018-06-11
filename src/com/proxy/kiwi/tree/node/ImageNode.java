package com.proxy.kiwi.tree.node;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.event.TreeEvent;

public class ImageNode extends Node{

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	List<URI> images;

    public ImageNode(TreeNode parent, Path path) throws NodeException {
        super(parent, path);
        images = new LinkedList<>();
    }

    @Override
    protected void buildInternal() {
        try {
            Files.list(getPath())
            	.filter(FolderNode::isImage)
            	.map(Path::toUri)
            	.forEach(images::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept(TreeEvent event) {

    }

    @Override
    public void prune() {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public Stream<Path> getImages() {
        return images.stream()
        		.map(Paths::get);
    }

    @Override
    public Stream<Node> getChildren() {
        return Stream.empty();
    }

    @Override
    public String toString() {
        return super.toString() + " " + images.size() + " images";
    }

    public Path before(Path path) {
        for (int i = 0; i < images.size(); i++) {
            if (Paths.get(images.get(i)).equals(path)) {
                return Paths.get(images.get(Math.max(0,i-1)));
            }
        }

        return path;
    }

    public Path after(Path path) {
        for (int i = 0; i < images.size(); i++) {
            if (Paths.get(images.get(i)).equals(path)) {
                return Paths.get(images.get(Math.min(images.size()-1,i+1)));
            }
        }

        return path;
    }
}
