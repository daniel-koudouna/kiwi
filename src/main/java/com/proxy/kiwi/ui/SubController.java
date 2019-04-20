package com.proxy.kiwi.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.ResourceBundle;

import org.apache.tools.ant.util.JavaEnvUtils;

import com.proxy.kiwi.app.Kiwi;
import com.proxy.kiwi.instancer.LaunchParameters;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.tree.node.Node;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;

public class SubController extends AbstractController implements TileContainer {

    @FXML
    VBox root;
    @FXML
    Polygon arrow;
    @FXML
    Label lblName;
    @FXML
    FlowPane tilePane;
    @FXML
    Pane topPane;
    @FXML
    Pane bottomPane;

    FlowPane pane;
    Node node;
    javafx.scene.Node target;
    boolean open;
    private int pos;
    private double topHeight;
    private double bottomHeight;

    public SubController(FlowPane pane, Node node, javafx.scene.Node target) {
        this.pane = pane;
        this.node = node;
        this.target = target;
        this.pos = -1;
        this.open = true;
    }

    public boolean reposition() {
        int oldPos = this.pos;
        ObservableList<javafx.scene.Node> children = pane.getChildren();
        boolean found = false;
        boolean placed = false;
        double x = target.getLayoutX();

        if (children.contains(root)) {
            children.remove(root);
            pane.autosize();
        }

        for (int i = 0; i < children.size(); i++) {
            javafx.scene.Node n = children.get(i);
            if (n.getLayoutX() <= x && n != root && found) {
                children.add(i, root);
                placed = true;
                break;
            }
            if (n.equals(target)) {
                found = true;
            }
        }
        if (!placed) {
            children.add(root);
        }

        this.pos = children.indexOf(root);
        return this.pos != oldPos;
    }

    public void crop() {
        this.pos = pane.getChildren().indexOf(root);
        ObservableList<javafx.scene.Node> children = pane.getChildrenUnmodifiable();

        boolean cropTop = false;
        boolean cropBottom = false;

        int prevIndex = pos - 1;
        if (prevIndex > 0) {
            Pane previousPane = (Pane) children.get(prevIndex);
            while (prevIndex > 0 && !previousPane.isVisible()) {
                prevIndex--;
                previousPane = (Pane) children.get(prevIndex);
            }
            if (prevIndex > 0 && previousPane.getWidth() == root.getWidth()) {
                cropTop = true;
            }
        }

        int nextIndex = pos + 1;
        if (nextIndex < children.size()) {
            Pane nextPane = (Pane) children.get(nextIndex);
            while (nextIndex < children.size() && !nextPane.isVisible()) {
                nextPane = (Pane) children.get(nextIndex);
                nextIndex++;
            }
            if (nextIndex != children.size() && nextPane.getWidth() == root.getWidth()) {
                cropBottom = true;
            }
        }

        if (cropTop) {
            topPane.setPrefHeight(0.0);
        } else {
            topPane.setPrefHeight(topHeight);
        }
        if (cropBottom) {
            bottomPane.setPrefHeight(0.0);
        } else {
            bottomPane.setPrefHeight(bottomHeight);
        }

        pane.autosize();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.prefWidthProperty().bind(pane.widthProperty());
        lblName.setText(node.name);
        arrow.translateXProperty().bind(target.layoutXProperty().add(Explorer.reqW / 2.0));

        topHeight = topPane.getPrefHeight();
        bottomHeight = bottomPane.getPrefHeight();

        root.setVisible(false);
        reposition();

        node.getChildren().forEach(n -> {
            tilePane.getChildren().add(new Tile(this, n).component());
        });
        tilePane.autosize();
        pane.autosize();

        Platform.runLater(this::open);
    }

    @Override
    protected String path() {
        return "/fxml/sub.fxml";
    }

    @Override
    public void handleChildClick(Tile tile) {
        if (!tile.isVisible()) {
            return;
        }

        if (tile.node instanceof ImageNode) {
            String classpath = System.getProperty("java.class.path");
            String path = JavaEnvUtils.getJreExecutable("java");
            String tempDir = System.getProperty("java.io.tmpdir");

            Path tempFile = Paths.get(tempDir, "kiwi.tmp");

            System.out.println("WRITING TEMP FILE");

            LinkedList<ImageNode> nodeList = new LinkedList<>();

            node.stream().filter(n -> n.status.get().show()).filter(n -> n instanceof ImageNode)
                    .map(n -> ((ImageNode) n)).forEach(nodeList::add);

            Path initialPath = ((ImageNode) tile.node).getImages().findFirst().get();

            LaunchParameters params = new LaunchParameters(initialPath, nodeList);

            try {
                Files.createFile(tempFile);
            } catch (FileAlreadyExistsException e) {
                // OK
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                FileOutputStream fos = new FileOutputStream(tempFile.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(params);
                oos.close();
            } catch (FileNotFoundException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            } catch (IOException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, Kiwi.class.getName(),
                    tempFile.toString());
            processBuilder.inheritIO();
            try {
                processBuilder.start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return;
        }
    }

    @FXML
    public void close() {
        open = false;
        root.setVisible(false);
        root.setDisable(true);
        root.setPrefHeight(0.0);
        root.autosize();

    }

    public void open() {
        open = true;
        root.setVisible(true);
        root.setDisable(false);
        root.setPrefHeight(Control.USE_COMPUTED_SIZE);
        root.autosize();
    }

    public void toggle() {
        if (open) {
            close();
        } else {
            open();
        }
    }
}
