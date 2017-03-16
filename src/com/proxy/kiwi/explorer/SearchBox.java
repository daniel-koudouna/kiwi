package com.proxy.kiwi.explorer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.Folders;
import com.proxy.kiwi.core.utils.Command;
import com.proxy.kiwi.core.utils.Resources;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class SearchBox extends HBox{
	
	static final String TAG_PREFIX = ":";
	static final String FXML_FILE = "search_box.fxml";
	static final int MIN_TAG_LENGTH = 3;

	
	LinkedList<Folder> parents;
	LinkedList<String> tags;

	LinkedList<LabelTuple> labels;
	
	Set<String> allTags;
	
	IntegerProperty terms;
	
	private Runnable onSingleHandler;
	private Runnable onChange;
	Node onEmptyFocus;
	
	@FXML private TextField searchField;
	@FXML private HBox searchTags;
	
	public SearchBox() {
		loadLayout();
		Platform.runLater(this::init);
		init();
	}
	
	public void init() {
		Platform.runLater( ()-> {
			this.prefWidthProperty().bind(this.getScene().getWindow().widthProperty().subtract(300));
		});
		
		parents = new LinkedList<>();
		
		parents.add(Folders.getRoot());
		tags = new LinkedList<>();

		terms = new SimpleIntegerProperty(1);

		labels = new LinkedList<>();
		
		allTags = Config.getTags();
		
		searchField.setOnKeyPressed((event) -> {
			if (event.getCode().equals(KeyCode.TAB)) {
				//TODO autocomplete
			} else if (Config.getCommandFor(event.getCode()).equals(Command.BACK)
					&& searchField.textProperty().getValue().length() == 0) {
				signalBack();
			} else if (Config.getCommandFor(event.getCode()).equals(Command.ENTER)) {
				String search = searchField.textProperty().getValueSafe().toLowerCase().trim();
				if (search.startsWith(TAG_PREFIX) && search.length() > 1) {
					String tag = search.substring(TAG_PREFIX.length()).trim().toLowerCase();

					System.out.println(tag);
					long possibleTags = allTags.stream().filter(s -> !tags.contains(s)).filter(s -> s.toLowerCase().contains(tag)).count();
					System.out.println(possibleTags);
					boolean canAddTag = (possibleTags == 1);
					
					if (canAddTag) {
						String singleTag = allTags.stream().filter(s -> !tags.contains(s)).filter(s -> s.toLowerCase().contains(tag)).findFirst().get();
						tags.add(singleTag);
						addTagFolder(singleTag);
						tryChange();
						searchField.setText("");
					}

				}
				if (onSingleHandler != null) {
					onSingleHandler.run();
				}

			}
		});

	}
	
	@FXML
	public void handleInputKeyEvent(KeyEvent event) {
		if (searchField.getText().trim().length() == 0) {
			switch (Config.getCommandFor(event.getCode())) {
			case LEFT:
			case RIGHT:
			case DOWN:
			case UP:
				if (onEmptyFocus != null) {
					onEmptyFocus.requestFocus();
				}
				break;
			default:
				break;
			}
		}
		event.consume();
	}
	
	public void onEmptyFocus(Node node) {
		this.onEmptyFocus = node;
	}
	
	public void onSingleInteract(Runnable r) {
		onSingleHandler = r;
	}
	
	public boolean accept(FolderPanel panel, boolean collapse, HashMap<String, Boolean> visiblePaths) {
		allTags = Config.getTags();

		
		Folder folder = panel.folder;
		Set<String> folderTags = Config.getTags(folder);

		if (folder.getParent() == null) {
			return false;
		}

		// Check that path is visible
		for (Entry<String,Boolean> entry : visiblePaths.entrySet()) {
			if (folder.getFilenameProperty().get().contains(entry.getKey()) && !entry.getValue()) {
				return false;
			}	
		}

		
		String search = searchField.textProperty().getValueSafe().toLowerCase().trim();
		
		collapse = collapse || ( search.startsWith(TAG_PREFIX) && search.length() > MIN_TAG_LENGTH) || !tags.isEmpty();
		
		boolean parentValid = (parents.isEmpty() || folder.getParent().equals(parents.getLast()));
		boolean anscestorValid = (parents.isEmpty() || folder.hasAncestor(parents.getLast()));

		boolean folderValid = (collapse && anscestorValid) || (!collapse && parentValid);

		boolean tagsValid = (tags.isEmpty() || folderTags.containsAll(tags));

		Set<String> remainingTags = folderTags.stream()
				.filter( (s) -> (!tags.contains(s)))
				.collect(Collectors.toSet());
		
		boolean searchValid = false;
		
		if (search.startsWith(TAG_PREFIX)) {
			String tSearch = search.substring(TAG_PREFIX.length());
			searchValid = remainingTags.stream().anyMatch((s) -> s.toLowerCase().contains(tSearch));
		} else {
			if (collapse) {
				searchValid = search.length() == 0 || folder.getName().toLowerCase().contains(search) || folder.hasAncestorWith(search);
			} else {
				searchValid = search.length() == 0 || folder.getName().toLowerCase().contains(search);
			}

		}
		
		boolean isValid = folderValid && tagsValid && searchValid;

		return isValid;
	}

	public StringProperty getQuery() {
		return searchField.textProperty();
	}

	public IntegerProperty getTerms() {
		return terms;
	}
	
	private void addFolder(String name) {
		Label l = new Label(name);
		labels.add(new LabelTuple(LabelType.FOLDER, l));
		searchTags.getChildren().add(searchTags.getChildren().size() - 1, l);
		tryChange();
	}

	private void removeLastFolder() {
		if (parents.size() > 1) {
			parents.removeLast();
			for (int i = labels.size() -1 ; i >= 0; i--) {
				if (labels.get(i).type == LabelType.FOLDER) {
					searchTags.getChildren().remove(labels.get(i).label);
					labels.remove(i);
					tryChange();
					break;
				}
			}
		}
	}

	private void addTagFolder(String name) {
		Label l = new Label(name);
		labels.add(new LabelTuple(LabelType.TAG, l));
		searchTags.getChildren().add(searchTags.getChildren().size() - 1, l);
		tryChange();
	}

	private void removeLastTag() {
		if (!tags.isEmpty()) {
			tags.removeLast();
			for (int i = labels.size() -1 ; i >= 0; i--) {
				if (labels.get(i).type == LabelType.TAG) {
					searchTags.getChildren().remove(labels.get(i).label);
					labels.remove(i);
					tryChange();
					break;
				}
			}
		}
	}

	public synchronized void signal(Folder folder) {
		addFolder(folder.getName());
		parents.add(folder);
		searchField.setText("");
	}

	public synchronized void signalBack() {
		if (parents.size() > 1) {
			removeLastFolder();
		} else if (tags.size() > 0) {
			removeLastTag();
		}
		tryChange();
	}
	
	public void tryChange() {
		Platform.runLater(() -> {
			if (onChange != null) {
				onChange.run();
			}
		});
	}
	
	public void onChange(Runnable onChange) {
		this.onChange = onChange;
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

enum LabelType {
	TAG,FOLDER;
}

class LabelTuple {
	public LabelType type;
	public Label label;
	
	public LabelTuple(LabelType type, Label label) {
		super();
		this.type = type;
		this.label = label;
	}
}
