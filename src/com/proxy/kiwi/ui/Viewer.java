package com.proxy.kiwi.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.proxy.kiwi.app.Configuration;
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
  private List<ImageNode> flatTree;
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
  private Timeline translateTimeline;
  private Timeline idleTimeline;

  private boolean downPressed, upPressed;

  public Viewer(List<ImageNode> tree, Path initial, Stage stage, Configuration config) {
    super();
    this.stage = stage;
    this.flatTree = tree;
    this.current = new Dynamic<>(initial);
    this.timeline = new Timeline();

    this.downPressed = false;
    this.upPressed = false;

    Optional<ImageNode> presentNode = tree.stream().filter(n -> {
	return n.getImages().anyMatch(p -> p.equals(initial));
      }).findFirst();

    this.currentBranch = new Dynamic<>(presentNode.orElseThrow(() -> new RuntimeException()));
    this.cache = new ImageCache(config.viewer_cache_size);
    this.cache.start();

    this.translateTimeline = new Timeline
      (new KeyFrame
       (Duration.millis(10),
	ae -> {
	 double y = group.getTranslateY();
	 int delta = config.viewer_scroll_delta;
	 double scale = view.getScaleY();
	 if (this.upPressed && !this.downPressed) {
	   group.setTranslateY(Math.min(0, y + delta*scale));
	 } else if (this.downPressed && !this.upPressed) {
	   double maxDown = root.getHeight() - (scale*view.getFitHeight());
	   group.setTranslateY(Math.max(maxDown, y - delta*scale));
	 }
	}));

    this.idleTimeline = new Timeline
      (new KeyFrame
       (Duration.millis(config.viewer_idle_time),
	ae -> {
	 ImageNode in = currentBranch.get();
	 Path curr = current.get();
	 for (int i = 0; i < config.viewer_cache_size /2; i++) {
	   curr = in.after(curr);
	   if (!cache.containsQueued(curr)) {
	     cache.put(curr);
	     break;
	   }
	 }
	}
	));
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
    return getDelta(-1);
  }
  private ImageNode getNext() {
      return getDelta(1);
  }

  private ImageNode getDelta(int delta) {
    int currIndex = this.flatTree.indexOf(this.currentBranch.get());
    return flatTree.get(Math.min(this.flatTree.size() -1 , Math.max(0, currIndex + delta)));
  }

  @FXML
  public void onKeyUp(KeyEvent e) {
    switch (e.getCode()) {
    case W:
    case UP:
      this.upPressed = false;
      break;
    case S:
    case DOWN:
      this.downPressed = false;
      break;
    default:
      break;
    }
  }

  @FXML
  public void onKeyPress(KeyEvent e) {
    switch (e.getCode()) {
    case A:
    case J:
    case LEFT:
      this.current.update(currentBranch.get().before(this.current.get()));
      this.idleTimeline.stop();
      this.idleTimeline.play();
      break;
    case D:
    case K:
    case RIGHT:
      this.current.update(currentBranch.get().after(this.current.get()));
      this.idleTimeline.stop();
      this.idleTimeline.play();
      break;
    case H:
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
      this.upPressed = true;
      break;
    case S:
    case DOWN:
      this.downPressed = true;
      break;
    case EQUALS:
      view.setScaleX(view.getScaleX() + 0.1);
      view.setScaleY(view.getScaleY() + 0.1);
      break;
    case MINUS:
      view.setScaleX(view.getScaleX() - 0.1);
      view.setScaleY(view.getScaleY() - 0.1);
      break;
    case C:
      System.out.println(cache);
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
	timeline = new Timeline
	  (new KeyFrame
	   (Duration.millis(5),
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

    idleTimeline.setCycleCount(Animation.INDEFINITE);
    idleTimeline.play();

    translateTimeline.setCycleCount(Animation.INDEFINITE);
    translateTimeline.play();
  }
}
