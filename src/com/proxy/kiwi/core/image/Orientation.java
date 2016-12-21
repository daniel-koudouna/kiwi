package com.proxy.kiwi.core.image;

public enum Orientation {
	NONE(0), CW_HALF(90), CCW_HALF(270), FULL(180);

	private double rotation;

	Orientation(double rotation) {
		this.rotation = rotation;
	}

	public double getRotation() {
		return rotation;
	}

	public double getWidthRatio(double width, double height) {
		switch (this) {
		case CCW_HALF:
		case CW_HALF:
			return width / height;
		case FULL:
		case NONE:
		default:
			return 1.0;
		}
	}

	public double getHeightRatio(double width, double height) {
		switch (this) {
		case CCW_HALF:
		case CW_HALF:
			return height / width;
		case FULL:
		case NONE:
		default:
			return 1.0;

		}
	}
}
