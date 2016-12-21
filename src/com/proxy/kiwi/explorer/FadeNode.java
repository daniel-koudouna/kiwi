package com.proxy.kiwi.explorer;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.util.Duration;

class FadeNode extends Transition {

	Node node;
	boolean hide;

	public FadeNode(Node node, boolean hide) {
		this.node = node;
		this.hide = hide;
		setCycleDuration(Duration.millis(200));
	}

	@Override
	protected void interpolate(double frac) {
		node.setOpacity(hide ? 1 - frac : frac);
	}

}
