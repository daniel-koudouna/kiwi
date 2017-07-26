package com.proxy.kiwi.explorer;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.Thumbnails;
import com.proxy.kiwi.core.utils.Log;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class FolderPanel extends StackPane {

	public Label name;
	public ImageView view;
	public Folder folder;
	public Timeline timeline;

	final Animation hide, show;

	private boolean isAnimating = false;

	boolean hidden;
	boolean infoHidden;
	public int xOff, yOff;
	private FadeNode showInfo;
	private FadeNode hideInfo;

	private VBox vbox;

	public FolderPanel(Folder folder, KiwiExplorerPane pane) {
		setMinSize(Thumbnails.thumb_width, Thumbnails.thumb_height);
		this.folder = folder;
		view = new ImageView();

		view.setImage(Thumbnails.getLoading());
		getChildren().add(view);

		vbox = new VBox();
		vbox.setBackground(
				new Background(new BackgroundFill(new Color(0.0, 0.0, 0.0, 0.45), new CornerRadii(0), new Insets(0))));
		StackPane.setAlignment(vbox, Pos.BOTTOM_CENTER);

		vbox.setPrefSize(Thumbnails.thumb_width, 0.2 * Thumbnails.thumb_height);
		vbox.setMaxSize(Thumbnails.thumb_width, 0.2 * Thumbnails.thumb_height);

		name = new Label(folder.getName());
		name.setPrefWidth(Thumbnails.thumb_width);
		name.setPrefHeight(0.2 * Thumbnails.thumb_height);
		name.getStyleClass().addAll("text", "folder-title");

		vbox.getChildren().add(name);

		showInfo = new FadeNode(vbox, false);

		hideInfo = new FadeNode(vbox, true);

		getChildren().add(vbox);
		vbox.setOpacity(0.0);
		infoHidden = true;

		hidden = true;
		setOpacity(0.0);
		setDisable(true);
		setPrefSize(0, 0);
		setMaxSize(0, 0);
		setMinSize(0, 0);

		show = new FadeNode(this, false);

		hide = new FadeNode(this, true);

		hide.setOnFinished((event) -> {
			setPrefSize(0, 0);
			setMaxSize(0, 0);
			setMinSize(0, 0);
			isAnimating = false;
		});

		show.setOnFinished((event) -> {
			isAnimating = false;
		});

		setOnMouseClicked((event) -> {
			pane.handlePanelClick(event, folder);
		});

		setOnMouseEntered((event) -> {
			if (pane.selected != null) {
				pane.selected.deselect();
			}
			pane.setSelected(this);
			if (!hidden) {
				select();
			}
		});

		setOnMouseExited((event) -> {
			if (!hidden) {
				deselect();
			}
		});

		timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), (event) -> {
			if (Thumbnails.getCache().contains(Config.getFolderImage(folder))) {
				Image image = Thumbnails.getCache().get(Config.getFolderImage(folder));
				if (image.getProgress() == 1) {
					setImage(image);
				} else {
					image.progressProperty().addListener((ov, old_val, new_val) -> {
						new_val = new_val.doubleValue() * 100.0;
						if (new_val.doubleValue() == 100.0) {
							Log.print(Log.GUI, "Setting image for " + folder.getName());
							setImage(image);
						}
					});
				}

				timeline.stop();
			}
		}));

		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();

	}

	private void setImage(Image image) {
		view.setImage(image);

		xOff = Config.getFolderXOffset(folder.getName());
		yOff = Config.getFolderYOffset(folder.getName());

		Rectangle2D viewport = new Rectangle2D(xOff, yOff, Thumbnails.thumb_width, Thumbnails.thumb_height);
		view.setViewport(viewport);
		view.setPreserveRatio(true);
		view.setSmooth(true);
	}

	public void setHidden(boolean hidden) {
		if (hidden && !this.hidden) {
			if (!isAnimating) {
				isAnimating = true;
				hide.play();
			}
			setDisable(true);
			this.hidden = true;
		} else if (!hidden && this.hidden) {
			setPrefSize(Thumbnails.thumb_width, Thumbnails.thumb_height);
			setMaxSize(Thumbnails.thumb_width, Thumbnails.thumb_height);
			setMinSize(Thumbnails.thumb_width, Thumbnails.thumb_height);
			if (!isAnimating) {
				isAnimating = true;
				show.play();
			}
			setDisable(false);
			this.hidden = false;
		}
	}

	public void select() {
		if (!hidden && infoHidden) {
			showInfo.play();
			infoHidden = false;
		}

	}

	public void deselect() {
		if (!hidden && !infoHidden) {
			hideInfo.play();
			infoHidden = true;
		} else {
			vbox.setOpacity(0);
		}
	}
}
