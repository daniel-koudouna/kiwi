package com.proxy.kiwi.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.proxy.kiwi.image.KImage;
import com.proxy.kiwi.image.KMetadata;
import com.proxy.kiwi.tree.filter.NodeStatus;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Log;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Tile extends AbstractController {

    static Image folderExpanded, folderCollapsed;
    static {
        folderExpanded = new Image("image/minus_small.png");
        folderCollapsed = new Image("image/plus_small.png");
    }

    @FXML
    ImageView image;

    @FXML
    VBox root;

    @FXML
    Label lblText;

    Node node;

    FadeTransition showTileBox, hideTileBox;

    boolean hasLoadedImage;
    private final TileContainer parent;

    private boolean visible;

    private double reqW, reqH;

    private FadeTransition showRoot;

    private FadeTransition hideRoot;

    static final Image LOADING_IMAGE;

    static {
        LOADING_IMAGE = new Image("image/loading.gif");
    }

    public Tile(TileContainer parent, Node node) {
        super();
        this.parent = parent;
        this.node = node;
        hasLoadedImage = false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reqW = root.getPrefWidth();
        reqH = root.getPrefHeight();

        image.setImage(LOADING_IMAGE);
        image.scaleXProperty().set(0.2f);
        image.scaleYProperty().set(0.2f);

        lblText.setText(node.toString());

        float duration = 0.25f;

        root.setOpacity(0);

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

        double ratio = reqW / (reqH * 1.0);

        if (node.imagePath == null) {
            Log.error(Tile.class, "No image path found for " + node.name);
        } else {
            KMetadata data = new KMetadata(node.getPath());
            KImage im;
            if (data.width == 0 || data.height == 0) {
                im = new KImage(node.imagePath, reqW, -1, true, true, true);
            } else {
                int imW = data.width;
                int imH = data.height;
                double imRatio = imW / (imH * 1.0);

                double sf;

                if (imRatio > ratio) {
                    sf = reqH / (1.0 * imH);
                } else {
                    sf = reqW / (1.0 * imW);
                }
                sf += 0.001;

                im = new KImage(node.imagePath, sf * imW, sf * imH, true, true, true);
            }
            image.setImage(im);
            image.setViewport(new Rectangle2D(0, 0, reqW, reqH));
            image.scaleXProperty().set(1.0);
            image.scaleYProperty().set(1.0);
        }
        Platform.runLater(() -> {
            showRoot.play();
        });
    }

    @Override
    protected String path() {
        return "/fxml/tile.fxml";
    }

    @FXML
    public void onClick() {
        parent.handleChildClick(this);
    }

    public boolean isVisible() {
        return visible;
    }

}
