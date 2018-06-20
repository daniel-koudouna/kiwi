package com.proxy.kiwi.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.utils.Dynamic;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Viewer extends AbstractController {

	private Dynamic<Path> current;
	private LinkedList<ImageNode> flatTree;
	private Dynamic<ImageNode> currentBranch;

	@FXML
	public StackPane root;

	@FXML
	public ImageView view;

	@FXML
	public Group group;

	@FXML
	public Label pageNum;

	@FXML
	public VBox chapters;
	private Stage stage;

	public Viewer(LinkedList<ImageNode> tree, Path initial, Stage stage) {
		super();
		this.stage = stage;
		this.flatTree = tree;
		this.current = new Dynamic<>(initial);

		Optional<ImageNode> presentNode = tree.stream().filter(n -> {
			return n.getImages().anyMatch(p -> p.equals(initial));
		}).findFirst();

		this.currentBranch = new Dynamic<>(presentNode.orElseThrow(() -> new RuntimeException()));
	}

	private void setImage(Path path) {
		try {
			view.setImage(new Image(path.toUri().toURL().toExternalForm()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private ImageNode getPrevious() {
		return flatTree.get(Math.max(0, this.flatTree.indexOf(this.currentBranch.get()) - 1));
	}

	private ImageNode getNext() {
		return flatTree.get(Math.min(this.flatTree.size() - 1, this.flatTree.indexOf(this.currentBranch.get()) + 1));
	}

	@FXML
	public void onKeyPress(KeyEvent e) {
		switch (e.getCode()) {
		case A:
			this.current.update(currentBranch.get().before(this.current.get()));
			break;
		case D:
			this.current.update(currentBranch.get().after(this.current.get()));
			break;
		case K:
			this.currentBranch.update(getPrevious());
			break;
		case L:
			this.currentBranch.update(getNext());
			break;
		case Q:
			stage.close();
			break;
		default:
			break;
		}
	}

	@Override
	protected String path() {
		return "viewer.fxml";
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		view.fitWidthProperty().bind(root.widthProperty());
		view.fitHeightProperty().bind(root.heightProperty());

		this.current.onChange(this::setImage);
		this.currentBranch.onChange(node -> this.current.update(node.getImages().findFirst().get()));
		setImage(this.current.get());
	}
}
