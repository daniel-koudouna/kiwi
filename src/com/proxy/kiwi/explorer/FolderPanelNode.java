package com.proxy.kiwi.explorer;

import java.util.Optional;

public class FolderPanelNode {

	Optional<FolderPanel> panel;
	Optional<FolderPanelNode> previous, next;

	public FolderPanelNode() {
		this.previous = Optional.empty();
		this.next = Optional.empty();
	}
}
