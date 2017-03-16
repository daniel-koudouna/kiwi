package com.proxy.kiwi.explorer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.proxy.kiwi.app.KiwiApplication;
import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.Folders;
import com.proxy.kiwi.core.services.Thumbnails;
import com.proxy.kiwi.core.utils.Resources;
import com.proxy.kiwi.core.utils.Stopwatch;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


public class KiwiExplorerPane extends AnchorPane{

	static final String FXML_FILE = "explorer_pane.fxml";

	@FXML private SearchBox searchBox;
	
	@FXML private ScrollPane scrollPane;
	@FXML private FlowPane flowPane;
	@FXML private CheckBox collapseCheck;

	@FXML private Button optionsButton;
	
	@FXML private ScrollPane menu;

	@FXML private Label libraryButton;
	@FXML private VBox libraryMenu;

	@FXML private Label hotkeyButton;
	@FXML private VBox hotkeyMenu;
	
	@FXML public VBox loadingBox;
	@FXML public ProgressBar loadingBar;
	@FXML public Label loadingLabel;
	
	public FolderPanel selected;

	boolean showMenu = false;
	TranslateTransition menuAnimation;

	LinkedBlockingQueue<Folder> folderQueue;

	public double scrollSpeed;

	public FolderMenu contextMenu;

	private Stage stage;

	private HashMap<String,Boolean> visiblePaths;

	public KiwiExplorerPane(Stage stage, String path) {

		this.stage = stage;

		loadLayout();
		init();
	}

	public void init() {
		stage.setTitle("Kiwi");
		
		List<String> paths = Config.getLibraries().stream().filter(e -> e.getValue().getAsBoolean()).map(e -> e.getKey()).collect(Collectors.toList());
		visiblePaths = new HashMap<>();
		Config.getLibraries().forEach(e -> {
			visiblePaths.put(e.getKey(), e.getValue().getAsBoolean());
		});
		
		Folders.mergeRoot(paths);

		flowPane.getChildren().clear();
		folderQueue = new LinkedBlockingQueue<Folder>();

		menuAnimation = new TranslateTransition(Duration.millis(250), menu);

		setMenuParent(libraryButton, libraryMenu);

		setMenuParent(hotkeyButton, hotkeyMenu);

		searchBox.getQuery().addListener((obs, old, n) -> updateView());
		
		searchBox.onChange(this::updateView);

		searchBox.onSingleInteract( () -> {
			FolderPanel panel = getSingleVisible();
			if (panel != null) {
				interact(panel.folder);
			}
		});

		searchBox.onEmptyFocus(flowPane);
		
		resetLibraries();

		Config.getHotkeys().forEach((entry) -> {
			HBox hbox = new HBox();
			Label name = new Label(entry.getKey());
			name.getStyleClass().add("sub-menu-item");
			Label val = new Label(entry.getValue().getAsJsonArray().toString());
			val.getStyleClass().add("sub-menu-item");

			hbox.getChildren().addAll(name, val);
			hotkeyMenu.getChildren().add(hbox);
		});

		Platform.runLater(() -> {
			try {
				if (Folders.thread != null) {
					Folders.thread.join();
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			addPanels(Folders.getRoot());
			updateView();

			if (!flowPane.getChildren().isEmpty()) {
				selected = (FolderPanel) flowPane.getChildren().get(0);
				selected.select();
			}
		});

		scrollSpeed = Config.getIntOption("item_height") / 3.0;
	}

	public void toggleSubmenu(Pane submenu) {
		if (submenu.isManaged()) {
			submenu.setMaxHeight(0);
			submenu.setOpacity(0);
			submenu.setManaged(false);
		} else {
			submenu.setMaxHeight(libraryButton.getHeight());
			submenu.setOpacity(1);
			submenu.setManaged(true);
		}
	}

	@FXML
	public void handleKeyEvent(KeyEvent event) {
		if (contextMenu != null && contextMenu.isShowing()) {
			event.consume();
			return;
		}
		
		int size = flowPane.getChildren().size();
		List<Node> children = flowPane.getChildren();

		switch (Config.getCommandFor(event.getCode())) {
		case ENTER:
			interact(selected.folder);
			event.consume();
			break;
		case MINIMIZE:
			stage.setIconified(true);
			break;
		case FULL_SCREEN:
			stage.setFullScreen(!stage.isFullScreen());
			break;
		case EXIT:
			System.exit(0);
			break;
		case BACK:
			searchBox.signalBack();
			event.consume();
			break;
		case LEFT:
			for (int i = size - 1; i >= 0; i--) {
				if (children.get(i) == selected) {
					for (int j = 1; j < size; j++) {
						FolderPanel panel = (FolderPanel) children.get((i - j + size) % size);
						if (!panel.hidden) {
							selected.deselect();
							selected = panel;
							selected.select();
							checkInBounds();
							return;
						}
					}
				}
			}
			break;
		case RIGHT:
			for (int i = 0; i < size; i++) {
				if (children.get(i) == selected) {
					for (int j = 1; j < size; j++) {
						FolderPanel panel = (FolderPanel) children.get((i + j) % size);
						if (!panel.hidden) {
							selected.deselect();
							selected = panel;
							selected.select();
							checkInBounds();
							return;
						}
					}
				}
			}
			break;
		case UP:
			for (int i = size - 1; i >= 0; i--) {
				if (children.get(i) == selected) {
					for (int j = 1; j < size; j++) {
						FolderPanel panel = (FolderPanel) children.get((i - j + size) % size);
						if (!panel.hidden && selected.getLayoutX() == panel.getLayoutX()) {
							selected.deselect();
							selected = panel;
							selected.select();
							event.consume();
							checkInBounds();
							return;
						}
					}
				}
			}
			break;
		case DOWN:
			for (int i = 0; i < size; i++) {
				if (children.get(i) == selected) {
					for (int j = 1; j < size; j++) {
						FolderPanel panel = (FolderPanel) children.get((i + j) % size);
						if (!panel.hidden && selected.getLayoutX() == panel.getLayoutX()) {
							selected.deselect();
							selected = panel;
							selected.select();
							event.consume();
							checkInBounds();
							return;
						}
					}
				}
			}
			break;
		case OPTIONS:
			if (selected == null) {
				break;
			}
			Bounds b = selected.localToScene(selected.getBoundsInLocal());
			int x = (int) ( 0.5*(b.getMinX() + b.getMaxX()) + selected.getScene().getWindow().getX() );
			int y= (int) ( 0.5*(b.getMinY() + b.getMaxY()) + selected.getScene().getWindow().getY() );
			showContextMenu(selected.folder, x,  y);
			break;
		default:
			break;
		}

	}

	@FXML
	public void updateView() {
		flowPane.getChildren().forEach((comp) -> {
			FolderPanel child = (FolderPanel) (comp);
			updateChild(child);
		});
		flowPane.autosize();
	}

	public void updateLast() {
		FolderPanel lastChild = (FolderPanel) (flowPane.getChildren().get(flowPane.getChildren().size() - 1));
		updateChild(lastChild);
		flowPane.autosize();
	}

	private void updateChild(FolderPanel child) {
		if (searchBox.accept(child, collapseCheck.isSelected(), visiblePaths)) {
			child.setHidden(false);

			Thumbnails.requestExpress(child.folder);
		} else {
			child.setHidden(true);
		}
	}

	@FXML
	public void showMenu(MouseEvent event) {
		if (showMenu) {
			menuAnimation.setFromX(0);
			menuAnimation.setToX(menu.getWidth());
		} else {
			menuAnimation.setFromX(menu.getWidth());
			menuAnimation.setToX(0);
		}
		showMenu = !showMenu;
		menuAnimation.play();
	}

	public void handlePanelClick(MouseEvent event, Folder folder) {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			interact(folder);
		} else if (event.getButton().equals(MouseButton.SECONDARY)) {
			showContextMenu(folder, (int) event.getScreenX(), (int) event.getScreenY());
		}
	}

	public void showContextMenu(Folder folder, int x, int y) {
		if (contextMenu != null) {
			contextMenu.hide();
		}
		contextMenu = new FolderMenu(selected);
		contextMenu.show(selected, x, y);
	}
	
	public void selectLibrary(String path) {
		Config.setOption("path", path);

		init();

	}

	@FXML
	public void changeLibrary() {

		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Select Library location");
		dc.setInitialDirectory(new File(Config.getOption("path")));

		File selected = dc.showDialog(stage.getOwner());
		if (selected != null) {
			Config.setOption("path", selected.getAbsolutePath());
			Config.addLibrary(selected.getAbsolutePath());
			Folders.buildRoot(Config.getOption("path"));
			init();
		}
	}

	@FXML
	public void scroll(ScrollEvent event) {
		double v = scrollPane.getVvalue();
		double percent = scrollSpeed / (flowPane.getHeight() - scrollPane.getHeight());

		scrollPane.setVvalue(v + (event.getDeltaY() > 0 ? -(percent) : percent));
		event.consume();
	}

	public void setSelected(FolderPanel panel) {
		if (selected != null) {
			selected.deselect();
		}
		this.selected = panel;
	}

	private void resetLibraries() {
		for (Iterator<Node> iterator = libraryMenu.getChildren().iterator(); iterator.hasNext();) {
			Node node = iterator.next();
			
			if (node instanceof CheckBox) {
				iterator.remove();
			}
		}
		Config.getLibraries().forEach((entry) -> {
			CheckBox box = new CheckBox(entry.getKey());
			box.getStyleClass().add("sub-menu-item");
			box.setSelected(Config.getLibrary(entry.getKey()));
			box.setOnAction(e -> {
				e.consume();
			});
			box.selectedProperty().addListener((obs, oldval, newVal ) -> {
				Config.setLibrary(box.getText(),newVal);
				visiblePaths.put(box.getText(),newVal);
				updateView();
			});
			libraryMenu.getChildren().add(0, box);
		});
	}

	private void interact(Folder folder) {
		if (folder.hasSubfolders()) {
			searchBox.signal(folder);
			scrollPane.setVvalue(0.0);
			Thumbnails.requestExpress(folder);
		} else {
			Stopwatch.click("Starting new JVM");

			String file = folder.getVolumes().get(0).getFilename();

			KiwiApplication.startReader(file);
		}
	}

	private void addPanels(Folder folder) {
		folderQueue.clear();
		queueFolder(folder);

		Platform.runLater(() -> {
			new ThreadPanelGen(folderQueue, flowPane, this).start();
		});
	}

	private void queueFolder(Folder folder) {
		folderQueue.add(folder);
		folder.getSubfolders().forEach((child) -> {
			queueFolder(child);
		});
	}

	private void checkInBounds() {

	    Bounds viewport = scrollPane.getViewportBounds();
	    double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
	    double nodeMinY = selected.getBoundsInParent().getMinY();
	    double nodeMaxY = selected.getBoundsInParent().getMaxY();
	    double viewportMinY = (contentHeight - viewport.getHeight()) * scrollPane.getVvalue();
	    double viewportMaxY = viewportMinY + viewport.getHeight();
	    if (nodeMinY < viewportMinY) {
	    	scrollPane.setVvalue(nodeMinY / (contentHeight - viewport.getHeight()));
	    } else if (nodeMaxY > viewportMaxY) {
	    	scrollPane.setVvalue((nodeMaxY - viewport.getHeight()) / (contentHeight - viewport.getHeight()));
	    }
	}

	private void setMenuParent(Label parent, Pane child) {
		parent.setOnMouseClicked((event) -> {
			toggleSubmenu(child);
		});
		toggleSubmenu(child);

	}

	private FolderPanel getSingleVisible() {
		int found = 0;
		FolderPanel panel = null;
		for (Node node : flowPane.getChildren()) {
			FolderPanel p = (FolderPanel) (node);
			if (!p.hidden) {
				panel = p;
				found++;
			}
		}

		if (found > 1) {
			return null;
		}

		return panel;
	}
	
	private void loadLayout() {
		FXMLLoader loader = new FXMLLoader(Resources.get(FXML_FILE));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
