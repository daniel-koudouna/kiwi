package com.proxy.kiwi.tree;

import java.util.stream.Stream;

import com.proxy.kiwi.tree.node.Node;

public abstract class TreeNode {
  public TreeNode parent;

  public abstract void prune();
  public abstract boolean isEmpty();
  public abstract Stream<Node> getChildren();

  public boolean hasChildren() {
    return getChildren().count() > 0;
  }

  public boolean hasParent(TreeNode parent) {
    return this.parent == parent || (this.parent != null && this.parent.hasParent(parent));
  }

  public boolean hasDirectParent(TreeNode parent) {
    return this.parent == parent || (this.parent != null && this.parent.parent != null && this.parent.parent.getChildren().count() == 1 && this.parent.hasDirectParent(parent));
  }
}
