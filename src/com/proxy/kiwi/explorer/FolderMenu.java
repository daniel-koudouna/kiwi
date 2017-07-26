package com.proxy.kiwi.explorer;

import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.proxy.kiwi.core.services.Config;

public class FolderMenu extends ContextMenu {

	FolderPanel panel;
	Menu tagMenu;

	public FolderMenu(FolderPanel panel) {
		this.panel = panel;

		if (!panel.folder.hasSubfolders()) {
			tagMenu = new Menu("Tags");

			Set<String> allTags = Config.getTags();

			Set<String> tags = Config.getTags(panel.folder);

			for (String tag : allTags) {
				CheckMenuItem item = new CheckMenuItem(tag);
				if (tags.contains(tag)) {
					item.setSelected(true);
				}
				item.setOnAction((e) -> {
					updateTags();
				});
				tagMenu.getItems().add(item);
			}

			getItems().add(tagMenu);

			MenuItem newTag = new MenuItem("Add new tag...");

			newTag.setOnAction((e) -> {
				addTag();
			});

			getItems().add(newTag);
		}

		MenuItem open = new MenuItem("Open in Explorer");
		open.setOnAction((e) -> {
			open();
		});
		getItems().add(open);

	}

	public void addTag() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setHeaderText("Add new tag");
		Optional<String> res = dialog.showAndWait();
		if (res.isPresent() && !res.get().trim().equals(" ")) {
			CheckMenuItem item = new CheckMenuItem(res.get().trim());
			item.setSelected(true);
			tagMenu.getItems().add(item);
			updateTags();
		}
	}

	public void updateTags() {
		Set<String> tags = new HashSet<>();
		for (MenuItem item : tagMenu.getItems()) {
			CheckMenuItem check = (CheckMenuItem) (item);
			if (check.isSelected()) {
				tags.add(check.getText());
			}
		}

		Config.setTags(panel.folder, tags);
	}

	public void open() {
		try {
			Desktop.getDesktop().open(new File(panel.folder.getFile().getAbsolutePath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void editTags() {
		EditMenu.show(panel.folder);
	}
}
