package com.proxy.kiwi.core.utils;

public class Tuple<X,Y> {
	final X x;
	final Y y;
	
	public Tuple(X x, Y y) {
		super();
		this.x = x;
		this.y = y;
	}

	public X getX() {
		return x;
	}

	public Y getY() {
		return y;
	}
	
	
}
