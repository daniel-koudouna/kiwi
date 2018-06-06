package com.proxy.kiwi.tree.filter;

import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Tuple;

import java.util.Arrays;
import java.util.List;

public class Filter {

    public static AbstractFilter tag(String tag) {
        return (node -> false);
    }

    public static AbstractFilter in(TreeNode parent) {
        return (node -> node.hasParent(parent));
    }

    public static AbstractFilter parent(TreeNode parent) {
        return (node -> node.hasDirectParent(parent));
    }

    public static AbstractFilter name(String name) {
        return (node -> node.getPath().getFileName().toString().contains(name));
    }

    public static AbstractFilter of(AbstractPipeFilter...filters) {
        return of(Arrays.asList(filters));
    }

    public static AbstractFilter of(List<AbstractPipeFilter> filters) {
        return (node -> {
            Tuple<Node, Boolean> t = new Tuple<>(node,true);
            for (AbstractPipeFilter filter : filters) {
                t = filter.apply(t);
            }
            return t.y;
        });
    }

    public static AbstractPipeFilter or(AbstractFilter filter) {
        return (t -> {
            return new Tuple<Node,Boolean>(t.x,t.y || filter.apply(t.x));
        });
    }

    public static AbstractPipeFilter and(AbstractFilter filter) {
        return (t -> {
            return new Tuple<Node,Boolean>(t.x,t.y && filter.apply(t.x));
        });
    }

}
