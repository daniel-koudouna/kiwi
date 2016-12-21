package com.proxy.kiwi.explorer;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.services.Thumbnails;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.util.HashSet;
import java.util.Set;


public class EditController {	
	@FXML private TextField tagInput;
	@FXML private TextField artistInput;
	
	@FXML private ChoiceBox<String> tagChoice;
	@FXML private ChoiceBox<String> artistChoice;
	
	@FXML private FlowPane tagPane;
	@FXML private FlowPane artistPane;
	
	@FXML private ImageView imageView;
	
	Folder folder;

	public void init(Folder folder) {
		this.folder = folder;

		Set<String> allTags = Config.getTags();
		// Set<String> allArtists = Settings.getArtists();

		Set<String> tags = Config.getTags(folder);
		// Set<String> artists = Settings.getArtists(folder);

		imageView.setImage(Thumbnails.getCache().get(folder.getImagePath()));

		System.out.println(tags.size());
		for (String tag : tags) {
			addTagToPane(tag);
		}

		tagChoice.getItems().addAll(allTags);

		tagChoice.setOnAction((e) -> {
			addChoiceTag();
		});
	}

	@FXML
	public void addTag() {
		String tag = tagInput.getText();
		addTagToPane(tag);
	}

	@FXML
	public void addChoiceTag() {
		String tag = tagChoice.getValue();
		addTagToPane(tag);
	}

	@FXML
	public void save() {
		Config.setTags(folder, getTags());
	}

	@FXML
	public void cancel() {
		EditMenu.get().hide();
	}

	private void addTagToPane(String tag) {
		tag = tag.toLowerCase().trim();
		for (Node node : tagPane.getChildren()) {
			Label label = (Label) (node);
			if (label.getText().equals(tag)) {
				return;
			}
		}
		Label label = new Label(tag);
		label.setOnMouseClicked((e) -> {
			tagPane.getChildren().remove(label);
		});
		tagPane.getChildren().add(label);
	}

	private Set<String> getTags() {
		Set<String> tags = new HashSet<>();
		tagPane.getChildren().forEach((node) -> {
			tags.add(((Label) (node)).getText());
		});
		return tags;
	}

	// private Set<String> getArtists() {
	// Set<String> artists = new HashSet<>();
	// artistPane.getChildren().forEach( (node) -> {
	// artists.add( ((Label)(node)).getText());
	// });
	// return artists;
	// }
}
