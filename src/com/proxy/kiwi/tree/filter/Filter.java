package com.proxy.kiwi.tree.filter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Tuple;

public class Filter {

    public static AbstractFilter tag(String tag) {
        return new NamedFilter("tag:" + tag , (node -> false));
    }

    public static AbstractFilter in(TreeNode parent) {
        return new NamedFilter("in:" + parent.toString(), (node -> node.hasParent(parent)));
    }

    public static AbstractFilter parent(TreeNode parent) {
        return new NamedFilter("parent:" + parent.toString(), (node -> node.hasDirectParent(parent)));
    }

    public static AbstractFilter name(String name) {
    	Function<Node, Boolean> fn = (node -> node.getPath().getFileName().toString().toLowerCase().contains(name.toLowerCase()));
        return new NamedFilter("name:" + name, (node -> fn.apply(node) || node.getChildren().anyMatch(fn::apply)));
    }

    public static AbstractFilter of(AbstractPipeFilter...filters) {
        return of(Arrays.asList(filters));
    }

    public static AbstractFilter of(List<AbstractPipeFilter> filters) {
        return (node -> {
            Tuple<Node, Boolean> t = new Tuple<>(node,false);
            for (AbstractPipeFilter filter : filters) {
                t = filter.apply(t);
            }
            return t.y;
        });
    }

    public static AbstractPipeFilter or(AbstractFilter filter) {
        return new NamedPipeFilter(filter.toString(), t -> {
            return new Tuple<Node,Boolean>(t.x,t.y || filter.apply(t.x));
        });
    }

    public static AbstractPipeFilter and(AbstractFilter filter) {
        return new NamedPipeFilter(filter.toString(), t -> {
            return new Tuple<Node,Boolean>(t.x,t.y && filter.apply(t.x));
        });
    }

}
