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

public class Tree extends TreeNode implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List<Node> children;

    public Tree(List<Node> children) {
        this.children = children;
        this.children.forEach(child -> child.parent = this);
    }

    public Tree(Node... children) {
        this(Arrays.asList(children));
    }

    @Override
    public void prune() {
        this.children.forEach(Node::prune);
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
        prune();
    }

    private static Tree saveNewTree(String name, List<Node> nodes) {
        System.out.println("NO TREE FOUND, MAKING NEW TREE AND SAVING");
        Tree tree = new Tree(nodes);
        tree.build();
        String filename = name + "__" + Integer.toHexString(tree.hashCode());
        Path filePath = Paths.get(Configuration.TEMP_PATH.toString(), filename);

        System.out.println("SAVING TREE");
        try {
            FileOutputStream fos = new FileOutputStream(filePath.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(tree);
            oos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return tree;
    }

    public static Tree from(List<Node> nodes) {
        String name = "";
        for (Node n : nodes) {
            name += n.name;
        }
        String finalName = "__tree__" + name;

        List<Path> possibleTrees = new LinkedList<>();
        try {
            possibleTrees = Files.list(Configuration.TEMP_PATH)
            .filter(p -> p.getFileName().toString().contains(finalName))
            .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return saveNewTree(finalName, nodes);
        }
        if (possibleTrees.isEmpty()) {
            return saveNewTree(finalName, nodes);
        }

        Optional<Tree> hashedTree = possibleTrees.stream()
        .map(p -> {
            Optional<Tree> empty = Optional.empty();
            try {
                FileInputStream fis = new FileInputStream(p.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
                Tree tree  = (Tree) ois.readObject();
                ois.close();
                return Optional.of(tree);
            } catch (IOException e) {
                return empty;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return empty;
            }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

        if (hashedTree.isPresent()) {
            System.out.println("FOUND TREE");
            Tree found =  hashedTree.get();
            found.update();
            //TODO write back (in new thread???)
            return found;
        }

        return saveNewTree(finalName, nodes);
    }
}
