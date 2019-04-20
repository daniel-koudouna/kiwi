package com.proxy.kiwi.instancer;

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedList;

import com.proxy.kiwi.tree.node.ImageNode;

public class LaunchParameters implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public final URI initial;
	public final LinkedList<ImageNode> nodelist;

	public LaunchParameters(Path initial, LinkedList<ImageNode> nodelist) {
		this.initial = initial.toUri();
		this.nodelist = nodelist;
	}
}
