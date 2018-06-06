package com.proxy.kiwi.tree.event;

import com.proxy.kiwi.tree.node.Node;

public class ChildAdded extends TreeEvent {
    public final Node child, parent;
    public final int index;

    public ChildAdded(Node child, Node parent, int index) {
        this.child = child;
        this.parent = parent;
        this.index = index;
    }
}
