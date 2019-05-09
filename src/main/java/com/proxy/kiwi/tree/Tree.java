package com.proxy.kiwi.tree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proxy.kiwi.app.Configuration;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Log;

public class Tree extends TreeNode implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    List<Node> children;
    final String name;
    private boolean serializable;

    public Tree(List<Node> children) {
        this.children = children;
        this.children.forEach(child -> child.parent = this);
        this.name = Tree.nameFrom(children);
        this.serializable = true;
    }

    public Tree(Node... children) {
        this(Arrays.asList(children));
    }

    /**
     * Use a hash code for each folder path to avoid
     * saving trees with the same name for different folders, such as
     * ~/Pictures and ~/Dropbox/Pictures
     */
    private static String nameFrom(List<Node> nodes) {
        StringBuilder nameBuilder = new StringBuilder();
        nodes.stream().forEach(n -> {
            nameBuilder.append(n.name);
            nameBuilder.append(Integer.toHexString(n.getPath().hashCode()));
        });
        return nameBuilder.toString();
    }

    /**
     * After pruning, the data structure will be out of sync with its file representation.
     * Therefore, it should not be serialized after pruning.
     */
    @Override
    public void prune() {
        this.children.forEach(Node::prune);
        this.serializable = false;
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public Stream<Node> getChildren() {
        return children.stream();
    }

    public Stream<Node> stream() {
        return children.stream().flatMap(c -> c.stream());
    }

    public void build() {
        LinkedList<Node> toBuild = new LinkedList<>();
        toBuild.addAll(children);

        while (!toBuild.isEmpty()) {
            Node node = toBuild.pop();
            node.build();
        }
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(children);
        return hash;
    }

    @Override
    public void update() {
        children.forEach(Node::update);
    }

    private static Optional<Tree> load(Path path) {
        try {
            FileInputStream fis = new FileInputStream(path.toFile());
            ObjectInputStream ois = new ObjectInputStream(fis);
            Tree tree  = (Tree) ois.readObject();
            ois.close();
            return Optional.of(tree);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void save() {
        if (!this.serializable) {
            throw new RuntimeException("Attempted to serialize out of sync data structure");
        }
        try {
            Path filePath = Paths.get(Configuration.TEMP_PATH.toString(), name + ".tree");
            FileOutputStream fos = new FileOutputStream(filePath.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Tree saveNewTree(List<Node> nodes) {
        Log.debug(Tree.class, "No Tree found, creating new Tree");
        Tree tree = new Tree(nodes);
        tree.build();
        tree.save();
        return tree;
    }

    public static Tree from(List<Node> nodes) {
        String finalName = nameFrom(nodes);

        List<Path> possibleTrees = new LinkedList<>();
        try {
            possibleTrees = Files.list(Configuration.TEMP_PATH)
            .filter(p -> p.getFileName().toString().contains(finalName))
            .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return saveNewTree(nodes);
        }
        if (possibleTrees.isEmpty()) {
            return saveNewTree(nodes);
        }

        Optional<Tree> hashedTree = possibleTrees.stream()
        .map(Tree::load)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

        if (hashedTree.isPresent()) {
            Log.debug(Tree.class, "Found existing Tree, updating");
            Tree found =  hashedTree.get();
            found.update();
            found.save();
            return found;
        } else {
            return saveNewTree(nodes);
        }
    }
}
