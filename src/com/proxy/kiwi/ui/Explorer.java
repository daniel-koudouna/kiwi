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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.tools.ant.util.JavaEnvUtils;

import com.proxy.kiwi.app.Kiwi;
import com.proxy.kiwi.instancer.LaunchParameters;
import com.proxy.kiwi.tree.Tree;
import com.proxy.kiwi.tree.event.ChildAdded;
import com.proxy.kiwi.tree.event.TreeBuilt;
import com.proxy.kiwi.tree.event.TreeEvent;
import com.proxy.kiwi.tree.filter.AbstractFilter;
import com.proxy.kiwi.tree.filter.AbstractPipeFilter;
import com.proxy.kiwi.tree.filter.Filter;
import com.proxy.kiwi.tree.filter.NodeStatus;
import com.proxy.kiwi.tree.node.ImageNode;
import com.proxy.kiwi.tree.node.Node;
import com.proxy.kiwi.utils.TaskQueue;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Explorer extends AbstractController {
    @FXML
    FlowPane filterPane;
    @FXML
    FlowPane tilePane;
    @FXML
    TextField searchBox;
    @FXML
    GridPane topPane;
    @FXML
    Pane fillerPane;
    @FXML
    Pane scrollControl;
    @FXML
    Pane scrollBar;

    Tree root;
    Tile activeTile;

    AbstractPipeFilter tempRootFilter, rootFilter;
    Optional<AbstractPipeFilter> queryFilter;
    List<AbstractPipeFilter> activeFilters;

    TaskQueue updateTask;

    Stage stage;

    public static final int reqW = 200;
    public static final int reqH = 300;
    private List<TileComponent> tiles;

    public Explorer(Tree root, Stage stage) {
        super();
        this.root = root;
        this.stage = stage;

        tiles = new ArrayList<>();

        rootFilter = Filter.or(Filter.parent(root));
        tempRootFilter = Filter.and(Filter.parent(root));
        queryFilter = Optional.empty();
        activeFilters = new LinkedList<>();
        updateTask = new TaskQueue();
        updateTask.start();

        onExit(updateTask::join);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.onEvent(this::handleEvent);

        Thread buildThread = new Thread( () -> {
            root.build();
            root.prune();
        });

        buildThread.start();
        searchBox.textProperty().addListener((obs, oldVal, newVal) -> onSearch(newVal));

        fillerPane.prefWidthProperty().bind(topPane.widthProperty());
        fillerPane.prefHeightProperty().bind(topPane.heightProperty());

        ChangeListener<Number> stageSizeListener = (observable, prev, curr) -> {
        	scroll();
        };

        stage.widthProperty().addListener(stageSizeListener);
    }

    private void translateYInBounds(Pane pane, double offset) {
    	pane.setTranslateY(Math.min(0, Math.max(-pane.getBoundsInLocal().getHeight() + pane.getHeight(), offset)));
    }

    private void scroll() {
    	double percent = -scrollControl.getTranslateY()/(scrollBar.getBoundsInLocal().getHeight());
    	translateYInBounds(tilePane,percent*(tilePane.getBoundsInLocal().getHeight() + tilePane.getHeight()));
    }

    private void updateScrollComponent() {
    	double percent = -tilePane.getTranslateY()/(tilePane.getBoundsInLocal().getHeight());
    	scrollControl.setTranslateY(Math.max(0,Math.min(scrollBar.getHeight(), percent*scrollBar.getHeight())));
    }

    @FXML
    public void onScrollControlDragged(MouseEvent event) {
    	double y = event.getY();
    	double delta = scrollControl.getHeight()/2.0 - y;
    	scrollControl.setTranslateY(Math.max(0, Math.min(scrollBar.getHeight() - scrollControl.getHeight(), scrollControl.getTranslateY() - delta)));
    	scroll();
    }

    @FXML
    public void onScrollbarClick(MouseEvent event) {
    	double pos = event.getY() - scrollControl.getHeight()/2.0;
    	scrollControl.setTranslateY(Math.max(0, Math.min(scrollBar.getHeight() - scrollControl.getHeight(), pos)));
    	scroll();
    }

    public synchronized void handleEvent(TreeEvent event) {
        if (event instanceof ChildAdded) {
            Node parent = ((ChildAdded) event).parent;
            Node child = ((ChildAdded) event).child;
            int index = ((ChildAdded) event).index;

            for (int i = 0; i < tiles.size(); i++) {
                TileComponent tc = tiles.get(i);
                if(tc.tile.node == parent) {
                    TileComponent tt = new TileComponent(new Tile(this,child), this::createPane);
                    tiles.add(i + index, tt);
                    return;
                }
            }

            TileComponent t = new TileComponent(new Tile(this,child), this::createPane);
            tiles.add(t);
        }
        if (event instanceof TreeBuilt) {
            updateView();
        }
    }

    public void createPane(TileComponent tc) {
        for (int i = 0; i < tiles.size(); i++) {
            TileComponent other = tiles.get(i);
            if (other == tc) {
                ObservableList<javafx.scene.Node> children = tilePane.getChildren();

                for (int nextIndex = i + 1; nextIndex < tiles.size(); nextIndex++) {
                    TileComponent next = tiles.get(nextIndex);
                    if (next.pane != null) {
                        Pane target = next.pane;
                        for (int j = 0; j < children.size(); j++) {
                            if (children.get(j) == target) {
                                children.add(j, tc.pane);
                                return;
                            }
                        }
                    }
                }

                children.add(tc.pane );
                return;
            }
        }
    }

    public void onEnter(Tile tile) {
        tiles.stream().map(tc -> tc.tile).forEach(t -> {
            if (t != tile) {
                t.hide();
            } else {
                t.show();
            }
        });
    }

    public void onExit(Tile tile) {
        tile.hide();
    }

    public void onMove(Tile tile) {
        onEnter(tile);
    }

    private void updateTilePane(double delta) {
    	translateYInBounds(tilePane, tilePane.getTranslateY() + delta);
    }

    private void updateTilePane() {
    	updateTilePane(0.0);
    }

    @FXML
    public void onScroll(ScrollEvent event) {
    	double y = 3.5*event.getDeltaY();
    	updateTilePane(y);
    	updateScrollComponent();
    }

    @FXML
    public void onKey(KeyEvent event) {
    	switch (event.getCode()) {
    	case BACK_SPACE:
    		if (activeFilters.isEmpty()) {
    			return;
    		}
    		activeFilters.remove(activeFilters.size() - 1);
    		updateView();
    		break;
    	default:
   			break;
    	}
    }

    public void onClick(Tile tile) {
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

    		root.stream()
    			.filter(n -> n.status.get() == NodeStatus.SHOW_SELF)
    			.filter(n -> n instanceof ImageNode)
    			.map(n -> ((ImageNode)n))
    			.forEach(nodeList::add);

    		Path initialPath = ((ImageNode)tile.node).getImages().findFirst().get();

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


    		ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, Kiwi.class.getName(), tempFile.toString());
    		processBuilder.inheritIO();
    		try {
				processBuilder.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    		return;
    	}

        AbstractPipeFilter filter = Filter.or(Filter.in(tile.node));

        activeFilters.forEach(f -> System.out.println(f));

        if (activeFilters.contains(filter)) {
        	activeFilters.remove(filter);
        } else {
            activeFilters.add(filter);
        }
        updateView();
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
        allFilters.add(rootFilter);
        allFilters.addAll(activeFilters);
        queryFilter.ifPresent(allFilters::add);
        if (activeFilters.isEmpty()) {
            allFilters.add(tempRootFilter);
        }

        AbstractFilter filter = Filter.of(allFilters);

        updateTask.enqueue(() -> {
            root.stream().forEach(n -> n.status.update(filter.apply(n) ? NodeStatus.SHOW_SELF : NodeStatus.HIDE));
        });
        tilePane.autosize();
    }

    @Override
    protected String path() {
        return "explorer.fxml";
    }

}

