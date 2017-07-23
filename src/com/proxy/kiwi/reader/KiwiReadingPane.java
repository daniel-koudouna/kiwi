package com.proxy.kiwi.reader;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

import com.proxy.kiwi.app.KiwiApplication;
import com.proxy.kiwi.core.image.KiwiImage;
import com.proxy.kiwi.core.image.Orientation;
import com.proxy.kiwi.core.services.Config;
import com.proxy.kiwi.core.utils.FXTools;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.utils.Resources;
import com.proxy.kiwi.core.utils.Stopwatch;
import com.proxy.kiwi.core.v2.folder.FolderV2;

import dorkbox.systemTray.SystemTray;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class KiwiReadingPane extends StackPane{

	static final String FXML_FILE = "reading_pane.fxml";

	@FXML private ImageView view;
	@FXML private Label pagenum;
	@FXML private VBox chapters;
	@FXML private Group group;

	private Rotate rotate;
	private Translate translate;

	private volatile FolderV2 folder;
	private KiwiImage image;
	private WritableImage fullImage;

	private Stage stage;

	private volatile SimpleIntegerProperty pageProperty = new SimpleIntegerProperty(1);
	private SimpleDoubleProperty zoomHeightRatio = new SimpleDoubleProperty(1.0);
	private SimpleDoubleProperty zoomWidthRatio = new SimpleDoubleProperty(1.0);
	private SimpleDoubleProperty heightRatio = new SimpleDoubleProperty(1.0);
	private SimpleDoubleProperty widthRatio = new SimpleDoubleProperty(1.0);

	private boolean isChangingPage;

	private int pageNumber;

	private Method resamplingMethod;

	private SimpleStringProperty titleProperty = new SimpleStringProperty();
	private SimpleStringProperty folderNameProperty = new SimpleStringProperty();

	// TODO move constants to separate place?
	private static final double ZOOM_RATIO_INCREMENT = 0.2, TRANSLATE_Y_RATE = 400;

	public KiwiReadingPane(Stage stage, String path) {

		this.stage = stage;

		resamplingMethod = Method.SPEED;


		stage.setWidth(Config.getIntOption("width"));
		stage.setHeight(Config.getIntOption("height"));


		folder = FolderV2.fromFile(path).orElseThrow(NullPointerException::new);
		folder.load();

		titleProperty.bind(new SimpleStringProperty("Kiwi - ").concat(folderNameProperty)
				.concat(" - ").concat(pageProperty.asString()));

		stage.titleProperty().bind(titleProperty);

		loadLayout();

		rotate = new Rotate(0);
		translate = new Translate(0, 0);

		group.getTransforms().addAll(rotate, translate);

		view.fitWidthProperty().bind(this.widthProperty().multiply(zoomWidthRatio).multiply(widthRatio));
		view.fitHeightProperty().bind(this.heightProperty().multiply(zoomHeightRatio).multiply(heightRatio));
		view.preserveRatioProperty().setValue(true);

		view.setSmooth(false);



		pageNumber = folder.find(path);

		pageProperty.set(pageNumber);


		pagenum.textProperty().bind(pageProperty.asString()
				.concat(new SimpleStringProperty("/").concat(folder.imageSize())));

		folderNameProperty.set(folder.getName());
		changePage(pageNumber);
		setChapters();

		Platform.runLater( () -> {
			ResizeListener listener = new ResizeListener(100) {

				@Override
				public void onResizeStart() {
					view.setImage(image);
				}

				@Override
				public void onResizeEnd() {
					loadImage();
				}

			};

			this.widthProperty().addListener(listener);
			this.heightProperty().addListener(listener);

			stage.getScene().setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					event.acceptTransferModes(TransferMode.MOVE);
				} else {
					event.consume();
				}
			});

			stage.getScene().setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					File file = db.getFiles().get(0);

					if (folder.contains(file)) {
						changePage(folder.find(file.getAbsolutePath()));
					} else {
						FolderV2 folder = FolderV2.fromFile(file.getAbsolutePath()).orElseThrow(NullPointerException::new);
						setFolder(folder);
						folder.load();
						changePage(folder.getStartPage());
					}
				}
			});

		});

		SystemTray.get().setStatus("Kiwi Reader - " + folder.getName());
	}

	@FXML
	public void handleKeyPress(KeyEvent event) {
		switch (Config.getCommandFor(event.getCode())) {
		case LEFT:
			changePage(pageProperty.get() - 1);
			break;
		case RIGHT:
			changePage(pageProperty.get() + 1);
			break;
		case NEXT_FOLDER:
			folder.next().ifPresent( next -> {
				Log.print(Log.EVENT, "Switching folder to " + next.getName());
				next.load();
				setFolder(next);
				changePage(1);				
			});;
			break;
		case PREVIOUS_FOLDER:
			folder.previous().ifPresent(previous -> {
				Log.print(Log.EVENT, "Switching folder to " + previous.getName());
				previous.load();
				setFolder(previous);
				changePage(1);				
			});;
			break;
		case CHAPTER_NEXT:
			if (Config.getChapters(folder.getName()) != null) {
				changePage(Config.getNextChapter(folder.getName(), pageProperty.get()));
			}
			break;
		case CHAPTER_PREVIOUS:
			if (Config.getChapters(folder.getName()) != null) {
				changePage(Config.getPreviousChapter(folder.getName(), pageProperty.get()));
			}
			break;
		case UP:
			addTranslateY(TRANSLATE_Y_RATE);
			break;
		case DOWN:
			addTranslateY(-TRANSLATE_Y_RATE);
			break;
		case ZOOM_IN:
			addZoom(ZOOM_RATIO_INCREMENT);
			break;
		case ZOOM_OUT:
			addZoom(-ZOOM_RATIO_INCREMENT);
			break;
		case CHAPTER_ADD:
			Config.addFolderChapter(folder.getName(), pageProperty.get());
			setChapters();
			break;
		case CHAPTER_REMOVE:
			Config.removeFolderChapter(folder.getName(), pageProperty.get());
			setChapters();
			break;
		case MINIMIZE:
			stage.setIconified(true);
			break;
		case QUALITY:
			resamplingMethod = (resamplingMethod == Method.QUALITY ? Method.SPEED : Method.QUALITY);
			Log.print(Log.EVENT, "Quality set to " + resamplingMethod.toString());
			break;
		case FULL_SCREEN:
			event.consume();
			stage.setFullScreen(!stage.isFullScreen());
			break;
		case EXIT:
			event.consume();
			stage.hide();
			KiwiApplication.exit();
			break;
		default:
			break;
		}
	}

	public IntegerProperty getPage() {
		return pageProperty;
	}

	public FolderV2 getFolder() {
		return folder;
	}

	public void setFolder(FolderV2 folder) {
		this.folder = folder;

		folderNameProperty.set(folder.getName());

		folder.getLoaded().onChange( (oldVal, newVal) -> {
			Platform.runLater( () -> {
				pagenum.textProperty().bind(pageProperty.asString()
						.concat(new SimpleStringProperty("/").concat(folder.imageSize())));				
			});
		});		

		folder.load();
		Log.print(Log.IO, "Found folder with " + folder.imageSize() + " images");

		
		setChapters();

		SystemTray.get().setStatus("Kiwi Reader - " + folder.getName());
	}

	public void loadImage() {

		double w = view.getFitWidth();
		double h = view.getFitHeight();

		int page = pageProperty.get();

		File file = folder.getImages().get(page - 1).getFile();

		try {
			// FIXME add .jpg compatibility
			BufferedImage image = ImageIO.read(file);

			BufferedImage result = Scalr.resize(image, resamplingMethod, Mode.AUTOMATIC, (int) w, (int) h,
					(BufferedImageOp[]) null);

			if (fullImage != null) {
				fullImage = null;
				System.gc();
			}
			fullImage = SwingFXUtils.toFXImage(result, null);

			view.setImage(fullImage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void changePage(int page) {
		if (page <= 0 || page > folder.imageSize() || isChangingPage) {
			return;
		}
		Stopwatch.click("Changing page");
		isChangingPage = true;
		Platform.runLater(() -> {

			this.pageProperty.set(page);

			File file = folder.getImages().get(page - 1).getFile();

			image = null;
			System.gc();

			Stopwatch.click("Loading Image");




			image = new KiwiImage(file);

			Orientation orientation = image.getOrientation();

			double width = image.getWidth();
			double height = image.getHeight();

			heightRatio.set(orientation.getHeightRatio(width, height));
			widthRatio.set(orientation.getWidthRatio(width, height));
			view.setRotate(orientation.getRotation());

			if (resamplingMethod != Method.SPEED) {
				loadImage();				
			} else {
				view.setImage(image);			
			}

			Stopwatch.click("Loading Image");			

			resetYOffset();

			isChangingPage = false;
			Stopwatch.click("Changing page");
		});
	}

	private void setChapters() {
		chapters.getChildren().clear();

		String[] chapternums = Config.getChapters(folder.getName());
		if (chapternums != null) {

			for (int i = 0; i < chapternums.length; i++) {
				ChapterLabel label = new ChapterLabel(chapternums[i],
						(i < chapternums.length - 1 ? chapternums[i + 1] : ""), pageProperty);

				chapters.getChildren().add(label);
			}
		}
	}

	private void resetYOffset() {
		translate.setY(getYLimit());
	}

	private void addZoom(double zoom) {
		zoomHeightRatio.set(zoomHeightRatio.get() + zoom);
		checkTranslateY();
	}

	private int getYLimit() {
		return (int) ((zoomHeightRatio.get() - 1) * this.getScene().getHeight() / 2);
	}

	private void checkTranslateY() {
		int lim = getYLimit();
		if (translate.getY() < -lim) {
			translate.setY(-lim);
		}
		if (translate.getY() > lim) {
			translate.setY(lim);
		}
	}

	private void addTranslateY(double dy) {
		if (zoomHeightRatio.get() >= 1) {

			translate.setY(translate.getY() + dy);
			checkTranslateY();
		}
	}

	private void loadLayout() {

		Object self = this;

		FXTools.runAndWait( () -> {
			Stopwatch.click("Loading Layout");
			FXMLLoader loader = new FXMLLoader(Resources.get(FXML_FILE));
			loader.setRoot(self);
			loader.setController(self);

			try {
				loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Stopwatch.click("Loading Layout");
		});
	}
}
