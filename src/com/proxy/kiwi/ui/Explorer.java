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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.tools.ant.util.JavaEnvUtils;

import com.proxy.kiwi.app.Kiwi;
import com.proxy.kiwi.instancer.LaunchParameters;
import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.TreeNode;
import com.proxy.kiwi.tree.filter.AbstractFilter;
import com.proxy.kiwi.tree.filter.AbstractPipeFilter;
import com.proxy.kiwi.tree.filter.Filter;
import com.proxy.kiwi.tree.filter.NodeStatus;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.Logger;
import com.proxy.kiwi.utils.TaskQueue;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Explorer extends AbstractController implements TileContainer {

    @FXML
    FlowPane tilePane;
    @FXML
    TextField searchBox;
    @FXML
    ScrollPane scrollPane;

    Tree root;
    Tile activeTile;

    AbstractPipeFilter tempRootFilter, rootFilter;
    Optional<AbstractPipeFilter> queryFilter;
    List<AbstractPipeFilter> activeFilters;

    List<TreeNode> pathNodes;

    TaskQueue updateTask;

    HashMap<TreeNode, SubController> subComponents;

    Stage stage;

    public static final int reqW = 100;
    public static final int reqH = 150;

    public Explorer(Tree root, Stage stage) {
        super();
        this.root = root;
        this.stage = stage;

        pathNodes = new ArrayList<>();
        rootFilter = Filter.or(Filter.root());
        tempRootFilter = Filter.and(Filter.root());
        queryFilter = Optional.empty();
        activeFilters = new LinkedList<>();
        updateTask = new TaskQueue();
        updateTask.start();
        subComponents = new HashMap<>();

        onExit(updateTask::join);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread buildThread = new Thread(() -> {
            root.prune();

            pathNodes.add(root);
            root.getChildren().forEach(pathNodes::add);
            root.getChildren().forEach(node -> {
                node.getChildren().forEach(n -> {
                    Platform.runLater(() -> {
                        tilePane.getChildren().add(new Tile(this, n).component());
                    });
                });
            });
            updateView();
        });

        tilePane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(30));

        buildThread.start();
        searchBox.textProperty().addListener((obs, oldVal, newVal) -> onSearch(newVal));

        scrollPane.widthProperty().addListener( (newV, oldV, obs) -> {
            subComponents.values().forEach(sc -> sc.root.prefWidthProperty().unbind());
            tilePane.autosize();
            //            subComponents.values().forEach(sc -> tilePane.getChildren().add(sc.component()));
            recalcOpenDialogs();
            subComponents.values().forEach(sc -> sc.root.prefWidthProperty().bind(tilePane.widthProperty()));
        });

    }

    private void recalcOpenDialogs() {
        Collection<SubController> cs = subComponents.values();
        boolean anyChanged = true;
        while (anyChanged) {
            anyChanged = false;
            for (SubController s : cs) {
                anyChanged = anyChanged || s.reposition();
            }
        }
        cs.forEach(s -> s.crop());
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

            root.stream().filter(n -> n.status.get().show()).filter(n -> n instanceof ImageNode)
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
        } else {
            Node n = tile.node;

            if (pathNodes.contains(n)) {
//                pathNodes.remove(n);
            } else {
                pathNodes.add(n);
            }

            if (!subComponents.containsKey(n)) {
                SubController sub = new SubController(tilePane, n, tile.component());
                Pane component = sub.component();
                component.visibleProperty().addListener( (newVal, oldVal, obs) -> {
                    recalcOpenDialogs();
                });
                subComponents.put(n, sub);
            } else {
                subComponents.get(n).toggle();
            }

            recalcOpenDialogs();

            updateView();
        }
    }

    public void onSearch(String query) {
        if (query.length() > 3) {
            queryFilter = Optional.of(Filter.and(Filter.name(query)));
        } else {
            queryFilter = Optional.empty();
        }

        updateView();
    }

    private void updateView() {
        LinkedList<AbstractPipeFilter> allFilters = new LinkedList<>();
        // allFilters.add(rootFilter);
        allFilters.add(Filter.or(Filter.path(pathNodes)));
        // allFilters.addAll(activeFilters);
        queryFilter.ifPresent(allFilters::add);

        // if (activeFilters.isEmpty()) {
        // allFilters.add(tempRootFilter);
        // }

        Logger.stream(allFilters, "Filters");

        AbstractFilter filter = Filter.of(allFilters);

        updateTask.enqueue(() -> {
            root.stream().forEach(n -> {
                boolean visible = filter.apply(n);
                boolean visibleChildren = n.getChildren().anyMatch(filter::apply);
                n.status.update(new NodeStatus(visible, visibleChildren));
            });
        });
        tilePane.autosize();
    }

    @Override
    protected String path() {
        return "new/main.fxml";
    }

}
