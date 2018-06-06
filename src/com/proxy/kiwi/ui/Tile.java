package com.proxy.kiwi.ui;

import com.proxy.kiwi.app.Kiwi;
import com.proxy.kiwi.image.KImage;
import com.proxy.kiwi.image.KMetadata;
import com.proxy.kiwi.tree.filter.NodeStatus;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.proxy.kiwi.tree.node.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Tile extends AbstractController{
    @FXML
    ImageView image;

    @FXML
    StackPane root;

    @FXML
    VBox tileBox;

    @FXML
    Label lblText;

    Node node;

    FadeTransition showTileBox, hideTileBox;

    boolean hasLoadedImage;
    private final Explorer parent;

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
        int reqW = Explorer.reqW;
        int reqH = Explorer.reqH;

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

        node.status.onChange(this::update);
        update(node.status.get());
    }

    private void update(NodeStatus status) {
        switch (status) {
            case SHOW_SELF:
                root.setOpacity(1);
                if (!hasLoadedImage) {
                    hasLoadedImage = true;
                    setImageWithDimensions();
                }
                break;
            case SHOW_CHILDREN:
            case HIDE:
                root.setOpacity(0.2);
                break;
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
