package com.proxy.kiwi.tree.filter;

import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Tuple;

public class NamedPipeFilter implements AbstractPipeFilter {
	public final String name;
	final AbstractPipeFilter filter;

	public NamedPipeFilter(String name, AbstractPipeFilter filter) {
		this.name = name;
		this.filter = filter;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NamedPipeFilter)) {
			return false;
		}
		return ((NamedPipeFilter)o).name.equals(this.name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Tuple<Node, Boolean> apply(Tuple<Node, Boolean> t) {
		return filter.apply(t);
	}


}
