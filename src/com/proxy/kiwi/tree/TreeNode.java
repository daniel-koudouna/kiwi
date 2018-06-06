package com.proxy.kiwi.tree;

import com.proxy.kiwi.tree.event.TreeEvent;
import com.proxy.kiwi.tree.node.Node;

import java.util.stream.Stream;

public abstract class TreeNode {
    public TreeNode parent;

    public abstract void accept(TreeEvent event);
    public abstract void emit(TreeEvent event);
    public abstract void prune();
    public abstract boolean isEmpty();
    public abstract Stream<Node> getChildren();

    public boolean hasParent(TreeNode parent) {
        return this.parent == parent || (this.parent != null && this.parent.hasParent(parent));
    }

    public boolean hasDirectParent(TreeNode parent) {
        return this.parent == parent || (this.parent != null && this.parent.parent != null && this.parent.parent.getChildren().count() == 1 && this.parent.hasDirectParent(parent));
    }
}
