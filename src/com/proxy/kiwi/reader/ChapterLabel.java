package com.proxy.kiwi.reader;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class ChapterLabel extends Label {

	int page, nextPage;

	public ChapterLabel(String pageStr, String nextPageStr, IntegerProperty currentPage) {
		super(pageStr);

		getStyleClass().add("text");

		try {
			this.page = Integer.parseInt(pageStr.trim());
		} catch (NumberFormatException e) {
			this.page = 0;
		}
		try {
			this.nextPage = Integer.parseInt(nextPageStr.trim());
		} catch (NumberFormatException e) {
			this.nextPage = Integer.MAX_VALUE;
		}

		refresh(currentPage.getValue());

		currentPage.addListener((obs, old, val) -> {
			refresh(val);
		});

	}

	public void refresh(Number pagenum) {
		if (pagenum.doubleValue() >= page && (pagenum.doubleValue() < nextPage)) {
			this.setTextFill(Color.CRIMSON);
		} else {
			this.setTextFill(Color.BLACK);
		}
	}
}
