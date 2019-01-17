package com.proxy.kiwi.tree.filter;

import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Tuple;

@FunctionalInterface
public interface AbstractPipeFilter {
    public Tuple<Node,Boolean> apply(Tuple<Node,Boolean> t);
}
