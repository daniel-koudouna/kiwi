package com.proxy.kiwi.explorer;

import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.utils.Resources;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class EditMenu extends Stage {
	static EditMenu instance;

	public static EditMenu get() {
		return instance;
	}

	public static void show(Folder folder) {
		if (instance != null) {
			instance.hide();
		}
		instance = new EditMenu(folder);
		instance.show();

	}

	Folder folder;

	public EditMenu(Folder folder) {
		this.folder = folder;
		try {
			FXMLLoader loader = new FXMLLoader(Resources.get("editmenu.fxml"));
			GridPane pane = loader.load();
			EditController controller = loader.getController();
			controller.init(folder);
			setScene(new Scene(pane, 900, 600));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
