package com.proxy.kiwi.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.proxy.kiwi.image.ImageCache;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.utils.Dynamic;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Viewer extends AbstractController {

	private Dynamic<Path> current;
	private LinkedList<ImageNode> flatTree;
	private Dynamic<ImageNode> currentBranch;
	private ImageCache cache;

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
	private Timeline timeline;

	final static int TRANSLATE_DELTA = 150;

	public Viewer(LinkedList<ImageNode> tree, Path initial, Stage stage) {
		super();
		this.stage = stage;
		this.flatTree = tree;
		this.current = new Dynamic<>(initial);
		this.timeline = new Timeline();

		Optional<ImageNode> presentNode = tree.stream().filter(n -> {
			return n.getImages().anyMatch(p -> p.equals(initial));
		}).findFirst();

		this.currentBranch = new Dynamic<>(presentNode.orElseThrow(() -> new RuntimeException()));
		this.cache = new ImageCache(5);
		this.cache.start();
	}

	private void setImage(Path path) {
		try {
			long then = System.nanoTime();
			view.setImage(new Image(path.toUri().toURL().toExternalForm()));
			long now = System.nanoTime();
			System.out.println("Time taken: " + (now - then)/1_000_000_000.0);
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
		case LEFT:
			this.current.update(currentBranch.get().before(this.current.get()));
			break;
		case D:
		case RIGHT:
			this.current.update(currentBranch.get().after(this.current.get()));
			break;
		case K:
			this.currentBranch.update(getPrevious());
			break;
		case L:
			this.currentBranch.update(getNext());
			break;
		case F:
			this.stage.setFullScreenExitHint("Press ESC or F to exit full-screen mode.");
			this.stage.setFullScreen(!this.stage.isFullScreen());
			break;
		case W:
		case UP:
			group.setTranslateY(Math.min(0,group.getTranslateY()+TRANSLATE_DELTA*view.getScaleY()));
			break;
		case S:
		case DOWN:
			double maxDown = root.getHeight() - (view.getScaleY()*view.getFitHeight());
			group.setTranslateY(Math.max(maxDown,group.getTranslateY()-TRANSLATE_DELTA*view.getScaleY()));
			break;
		case EQUALS:
			view.setScaleX(view.getScaleX() + 0.1);
			view.setScaleY(view.getScaleY() + 0.1);
			break;
		case MINUS:
			view.setScaleX(view.getScaleX() - 0.1);
			view.setScaleY(view.getScaleY() - 0.1);
			break;
		case X:
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


		this.current.onChange(p -> {
			long then = System.nanoTime();
			cache.put(p);
			timeline.stop();
			timeline = new Timeline(new KeyFrame(
			        Duration.millis(5),
			        ae -> {
			        	cache.get(p).ifPresent(im -> {
				        	timeline.stop();
				        	view.setImage(im);
				        	long now = System.nanoTime();
				        	System.out.println("Time taken: " + (now - then)/1_000_000_000.0);
			        	});
			        }));
			timeline.setCycleCount(Animation.INDEFINITE);
			timeline.play();
		});
//		this.current.onChange(this::setImage);

		this.currentBranch.onChange(node -> this.current.update(node.getImages().findFirst().get()));
		setImage(this.current.get());

		BiConsumer<Path, ImageNode> updateTextfn = ((Path p, ImageNode n) -> {
			pageNum.textProperty().set( (n.getImages().collect(Collectors.toList()).indexOf(p) + 1) + "/"+n.getImages().count());
		});
		Consumer<Path> updateTitle = (c -> stage.setTitle(c.getParent().getFileName() + " - " + c.getFileName()));

		current.onChange(c -> {
			updateTextfn.accept(c, currentBranch.get());
		});
		currentBranch.onChange(n -> {
			updateTextfn.accept(current.get(), n);
		});
		updateTextfn.accept(current.get(), currentBranch.get());
		updateTitle.accept(this.current.get());

		current.onChange(c -> group.setTranslateY(0));

		current.onChange(updateTitle::accept);

		view.scaleXProperty().addListener( (obs, prev, curr) -> {
			if (curr.doubleValue() > 1.0) {
				StackPane.setAlignment(group, Pos.TOP_CENTER);
			} else {
				StackPane.setAlignment(group, Pos.CENTER);
			}
		});
	}
}
