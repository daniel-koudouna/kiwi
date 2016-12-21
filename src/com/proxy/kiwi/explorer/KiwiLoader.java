package com.proxy.kiwi.explorer;

import com.proxy.kiwi.core.utils.Resources;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class KiwiLoader extends Preloader {

	public static Stage stage = null;

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane pane = (BorderPane) FXMLLoader.load(Resources.get("loading.fxml"));
		primaryStage.setScene(new Scene(pane, 600, 400));
		primaryStage.setResizable(false);

		primaryStage.initStyle(StageStyle.UNDECORATED);

		primaryStage.show();
		stage = primaryStage;
	}

	public static void hide() {
		Platform.runLater(() -> {
			if (stage != null) {
				stage.hide();
			}
		});
	}
}
