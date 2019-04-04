package com.proxy.kiwi.tree.filter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Tuple;

public class Filter {

  public static AbstractFilter tag(String tag) {
    return new NamedFilter("tag:" + tag , (node -> false));
  }

  public static AbstractFilter path(List<TreeNode> nodes) {
    return new NamedFilter("path:" + nodes, (node -> {
	  TreeNode parent = node.parent;
	  while (parent != null) {
	    if (!nodes.contains(parent)) {
	      return false;
	    }
	    parent = parent.parent;
	  }
	  return true;
	}));
  }

  public static AbstractFilter in(TreeNode parent) {
    return new NamedFilter("in:" + parent.toString(), (node -> node.hasDirectParent(parent)));
  }

  public static AbstractFilter root() {
    return new NamedFilter("root!", node -> {
	return (node.parent instanceof Tree) ||
	  (node.parent.parent != null && node.parent.parent instanceof Tree);
      });
  }

  public static AbstractFilter parent(TreeNode parent) {
    return new NamedFilter("parent:" + parent.toString(), (node -> node.hasDirectParent(parent)));
  }

  public static AbstractFilter name(String name) {
    Function<Node, String> sanitize = (node -> node.getPath().getFileName().toString().toLowerCase());
    Function<Node, Boolean> fn = (node -> {
	return node.stream().anyMatch(n -> sanitize.apply(n).contains(name.toLowerCase()));
      });
    return new NamedFilter("name:" + name, fn::apply);
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
    return new NamedPipeFilter("|| " + filter.toString(), t -> {
	return new Tuple<Node,Boolean>(t.x,t.y || filter.apply(t.x));
    });
  }

  public static AbstractPipeFilter and(AbstractFilter filter) {
    return new NamedPipeFilter("&& " + filter.toString(), t -> {
	return new Tuple<Node,Boolean>(t.x,t.y && filter.apply(t.x));
    });
  }

}
