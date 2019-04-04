package com.proxy.kiwi.tree.node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proxy.kiwi.utils.Tuple;

public class FolderNode extends Node {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public TreeSet<Node> children;
    private int internalHashCode;

    public static Optional<FolderNode> optional(Path path) {
        try {
            return Optional.of(new FolderNode(null, path));
        } catch (NodeException e) {
            System.err.println("Error opening " + path);
            return Optional.empty();
        }
    }

    public FolderNode(Node parent, Path path) throws NodeException {
        super(parent, path);
        children = new TreeSet<>();
        internalHashCode = 0;
    }

    @Override
    protected void buildInternal() {
        try {
            if (Files.list(getPath()).anyMatch(Node::isImage)) {
                Node self = new ImageNode(this, getPath());
                children.add(self);
            }
            Files.list(getPath()).sorted().forEach(this::handleDir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        children.forEach(Node::build);
    }

    private void handleDir(Path path) {
        if (Files.isDirectory(path)) {
            try {
                boolean hasChildrenFolders = Files.list(path).anyMatch(Files::isDirectory);
                if (hasChildrenFolders) {
                    Node child = new FolderNode(this, path);
                    children.add(child);
                }
                boolean hasChildrenImages = Files.list(path).anyMatch(FolderNode::isImage);
                if (hasChildrenImages) {
                    Node child = new ImageNode(this, path);
                    children.add(child);
                }
            } catch (NodeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Stream<Node> stream() {
        return Stream.concat(Stream.of(this), this.children.stream().flatMap(Node::stream));
    }

    @Override
    public void prune() {
        for (Iterator<Node> outeri = this.children.iterator(); outeri.hasNext();) {
            Node child = outeri.next();
            child.prune();
        }
        ArrayList<Node> marked = new ArrayList<>();
        ArrayList<Tuple<FolderNode,Node>> markedAdd = new ArrayList<>();
        for (Iterator<Node> iterator = this.children.iterator(); iterator.hasNext();) {
            Node child = iterator.next();
            if (child instanceof FolderNode) {
                FolderNode folderNode = (FolderNode) child;
                if (folderNode.children.size() == 1) {
                    folderNode.children.forEach(c -> {
                        markedAdd.add(new Tuple<FolderNode,Node>(folderNode,c));
                    });
                    marked.add(child);
                }
            }
        }
        marked.forEach(n -> this.children.remove(n));
        markedAdd.forEach(t -> {
            FolderNode parent = t.x;
            Node n = t.y;
            n.setName(parent.name + " -- " + n.name);
            n.parent = this;
            this.children.add(n);
        });
    }

    // @Override
    // public void prune() {
    // children.forEach(TreeNode::prune);
    // for (Iterator<Node> iterator = children.iterator(); iterator.hasNext(); ) {
    // Node child = iterator.next();
    // if (child instanceof FolderNode) {
    // FolderNode folderNode = (FolderNode)child;
    // if (folderNode.children.size() == 1) {
    // //System.out.println(folderNode.path + " has one child");
    // }
    // }
    // }
    // }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public Stream<Node> getChildren() {
        return children.stream();
    }

    @Override
    public int hashCode() {
        if (internalHashCode == 0) {
            try {
                List<Path> files = Files.list(getPath()).collect(Collectors.toList());
                internalHashCode = files.hashCode();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return internalHashCode;
    }

    @Override
    public boolean isValidHashCode() {
        int old = internalHashCode;
        this.internalHashCode = 0;
        hashCode();
        int newCode = internalHashCode;
        this.internalHashCode = old;
        return newCode == old;
    }
}
