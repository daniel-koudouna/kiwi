package com.proxy.kiwi.tree.event;

import com.proxy.kiwi.tree.Tree;

public class TreeBuilt extends TreeEvent{
    public final Tree tree;

    public TreeBuilt(Tree tree) {
        this.tree = tree;
    }
}
