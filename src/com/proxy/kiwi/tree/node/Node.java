package com.proxy.kiwi.tree.node;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.event.TreeEvent;
import com.proxy.kiwi.tree.filter.NodeStatus;
import com.proxy.kiwi.utils.Dynamic;

public abstract class Node extends TreeNode implements Comparable, Serializable {
    public final URI uri;
    public String imagePath;
    public Dynamic<NodeStatus> status;
    boolean built;
    public String imagePathRaw;

    public Node(TreeNode parent, Path path) throws NodeException {
        this.parent = parent;
        if (!Files.exists(path)) {
            throw new NodeException("Tried to create node with non-existent path");
        }
        this.uri = path.toUri();
        this.status = new Dynamic<>(NodeStatus.HIDE);
        this.built = false;
        buildImage();
    }


    public final void build() {
        if (!built) {
            buildInternal();
            built = true;
        }
    }

    protected void buildImage() {
        try {
            Optional<Path> img = Files
                    .find(getPath(),10, (p, b) -> true)
                    .filter(Node::isImage)
                    .findFirst();

            img.ifPresent(p -> {
                try {
                    this.imagePath = p.toUri().toURL().toExternalForm();
                    this.imagePathRaw = p.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void buildInternal();


    public static boolean isImage(Path path) {
        String s = path.toString().toLowerCase();
        return s.endsWith(".jpg") || s.endsWith(".png") || s.endsWith(".jpeg");
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Node) {
            return ((Node) o).uri.compareTo(this.uri)*-1;
        } else {
            return 0;
        }
    }

    @Override
	public String toString() {
    	return getPath().getFileName().toString();
    }

    public Path getPath() {
        return Paths.get(uri);
    }

    @Override
	public void emit(TreeEvent event) {
       parent.emit(event);
    }

    public Stream<Node> stream() {
        return Stream.of(this);
    }
}
