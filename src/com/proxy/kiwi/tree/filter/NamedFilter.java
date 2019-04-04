package com.proxy.kiwi.tree.filter;

import com.proxy.kiwi.tree.node.Node;

public class NamedFilter implements AbstractFilter{
	public final String name;
	public final AbstractFilter filter;

	public NamedFilter(String name, AbstractFilter filter) {
		this.name = name;
		this.filter = filter;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NamedFilter)) {
			return false;
		}
		return ((NamedFilter)o).name.equals(this.name);
	}

	@Override
	public boolean apply(Node node) {
		return filter.apply(node);
	}
}
