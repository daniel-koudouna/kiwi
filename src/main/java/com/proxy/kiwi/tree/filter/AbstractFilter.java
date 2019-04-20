package com.proxy.kiwi.tree.filter;

import com.proxy.kiwi.tree.node.Node;

@FunctionalInterface
public interface AbstractFilter {
    public boolean apply(Node node);
}
