package com.proxy.kiwi.tree;

import com.proxy.kiwi.tree.event.ChildAdded;
import com.proxy.kiwi.tree.event.TreeBuilt;
import com.proxy.kiwi.tree.event.TreeEvent;
import com.proxy.kiwi.tree.node.Node;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Tree extends TreeNode implements Serializable {

    transient List<Consumer<TreeEvent>> callbacks;
    List<Node> children;

    public Tree(Node...children) {
        this.callbacks = new LinkedList<>();
        this.children = Arrays.asList(children);
        this.children.forEach(child -> child.parent = this);
    }

    public void onEvent(Consumer<TreeEvent> callback) {
        this.callbacks.add(callback);
    }

    @Override
    public void accept(TreeEvent event) {
       children.forEach(c -> c.accept(event));
    }

    @Override
    public void emit(TreeEvent event) {
        callbacks.forEach(c -> c.accept(event));
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

        onEvent(e -> {
            if (e instanceof ChildAdded) {
                toBuild.add(((ChildAdded) e).child);
            }
        });

        while (!toBuild.isEmpty()) {
            Node node = toBuild.pop();
            node.build();
        }

        emit(new TreeBuilt(this));
    }
}
