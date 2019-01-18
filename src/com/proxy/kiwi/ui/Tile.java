package com.proxy.kiwi.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.proxy.kiwi.app.Kiwi;
import com.proxy.kiwi.image.KImage;
import com.proxy.kiwi.image.KMetadata;
import com.proxy.kiwi.tree.filter.NodeStatus;
import com.proxy.kiwi.tree.node.Node;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Tile extends AbstractController{

  static Image folderExpanded, folderCollapsed;
  static {
    folderExpanded = new Image(Kiwi.resource("minus_small.png").toString());
    folderCollapsed = new Image(Kiwi.resource("plus_small.png").toString());
  }

  @FXML
  ImageView image;

  @FXML
  StackPane root;

  @FXML
  VBox tileBox;

  @FXML
  Label lblText;

  @FXML
  ImageView statusImage;

  Node node;

  FadeTransition showTileBox, hideTileBox;

  boolean hasLoadedImage;
  private final Explorer parent;

  private boolean visible;

  private int reqW;

  private int reqH;

  private FadeTransition showRoot;

  private FadeTransition hideRoot;

  static final Image LOADING_IMAGE;

  static {
    LOADING_IMAGE = new Image(Kiwi.resource("loading.gif").toString());
  }

  public Tile(Explorer parent, Node node) {
    super();
    this.parent = parent;
    this.node = node;
    hasLoadedImage = false;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    reqW = Explorer.reqW;
    reqH = Explorer.reqH;

    root.prefWidthProperty().set(reqW);
    root.prefHeightProperty().set(reqH);

    image.fitWidthProperty().bind(root.widthProperty());
    image.fitHeightProperty().bind(root.heightProperty());
    image.setImage(LOADING_IMAGE);
    image.scaleXProperty().set(0.2f);
    image.scaleYProperty().set(0.2f);

    tileBox.prefHeightProperty().bind(root.heightProperty().multiply(0.25));
    lblText.setText(node.getPath().getFileName().toString());

    float duration = 0.25f;

    showTileBox = new FadeTransition();
    showTileBox.setNode(tileBox);
    showTileBox.setDuration(Duration.seconds(duration));
    showTileBox.setFromValue(0);
    showTileBox.setToValue(1);

    hideTileBox = new FadeTransition();
    hideTileBox.setNode(tileBox);
    hideTileBox.setDuration(Duration.seconds(duration));
    hideTileBox.setFromValue(1);
    hideTileBox.setToValue(0);
    tileBox.setOpacity(0);

    showRoot = new FadeTransition(Duration.seconds(duration), root);
    showRoot.setFromValue(0);
    showRoot.setToValue(1);

    hideRoot = new FadeTransition(Duration.seconds(duration), root);
    hideRoot.setFromValue(1);
    hideRoot.setToValue(0);
    hideRoot.setOnFinished(e -> {
	root.setDisable(true);
	root.setMinSize(0, 0);
	root.setMaxSize(0, 0);
	root.setPrefSize(0, 0);
      });

    node.status.onChange(this::update);
    root.setOpacity(0.0);
    update(node.status.get());
  }

  private void update(NodeStatus status) {
    if (status.show()) {
      if (this.node.hasChildren()) {
	if (status.children()) {
	  statusImage.setImage(folderExpanded);
	  image.setOpacity(0.4);
	} else {
	  statusImage.setImage(folderCollapsed);
	  image.setOpacity(1.0);
	}
      } else {
	statusImage.setImage(null);
      }

      visible = true;
      if (!hasLoadedImage) {
	hasLoadedImage = true;
	setImageWithDimensions();
      } else {
	if (root.getOpacity() < 1) {
	  root.setDisable(false);
	  root.setMinSize(reqW, reqH);
	  root.setMaxSize(reqW, reqH);
	  root.setPrefSize(reqW, reqH);
	  showRoot.play();
	}
      }
    } else {
      visible = false;
      if (root.getOpacity() > 0) {
	hideRoot.play();
      }
    }
  }

  private void setImageWithDimensions() {
    int reqW = Explorer.reqW;
    int reqH = Explorer.reqH;

    double ratio = reqW/(reqH*1.0);

    if (node.imagePath == null) {
      //System.out.println(node.path + " has no image path");
    } else {
      KMetadata data = new KMetadata(node.imagePathRaw);
      KImage im;
      if (data.width == 0 || data.height == 0) {
	im = new KImage(node.imagePath,reqW,-1,true,true,true);
      } else {
	int imW = data.width;
	int imH = data.height;
	double imRatio = imW/(imH*1.0);

	double sf;

	if (imRatio > ratio) {
	  sf = reqH / (1.0*imH);
	} else {
	  sf = reqW / (1.0*imW);
	}
	sf += 0.001;

	im = new KImage(node.imagePath,sf*imW,sf*imH,true,true,true);
      }
      image.setImage(im);
      image.setViewport(new Rectangle2D(0,0,reqW,reqH));
      image.scaleXProperty().set(1.0);
      image.scaleYProperty().set(1.0);
    }
    Platform.runLater(() -> {
	showRoot.play();
      });
  }


  @Override
  protected String path() {
    return "tile.fxml";
  }


  @FXML public void onMouseEnter() {
    parent.onEnter(this);
  }

  @FXML public void onMouseExit() {
    parent.onExit(this);
  }

  @FXML public void onMove() {
    parent.onMove(this);
  }

  @FXML public void onClick() {
    parent.onClick(this);
  }

  public boolean isVisible() {
    return visible;
  }

  public void show() {
    if (tileBox == null) {
      return;
    }

    if (tileBox.getOpacity() < 1) {
      //showTileBox.setFromValue(tileBox.getOpacity());
      showTileBox.play();
    }
  }

  public void hide() {
    if (tileBox == null) {
      return;
    }

    if (tileBox.getOpacity() > 0) {
      /*
       * TODO
       * UX decision whether or not to start
       * from current opacity. Looks not sharp
       * enough without full transition.
       */
      //hideTileBox.setFromValue(tileBox.getOpacity());
      hideTileBox.play();
    }
  }
}
