package es.carlosmontero.javafx.components.imagegallery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * The Class ImageGallery. A class utility to open and generate a ImageViewer to
 * visualize a list of photos. The gallery opens in a StackPane over the window,
 * simulating a modal with transparency.
 * 
 * @author Carlos Montero Canabal
 * 
 */
public class ImageGallery extends VBox {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageGallery.class);

	private static final double COUNTER_HEIGHT = 30D;

	private static final double THUMBNAILS_HEIGHT = 80;

	private ExecutorService threadPool;

	/** The parent stage. */
	private final Window parentStage;

	/** The canvas width. */
	private int canvasWidth;

	/** The factor. */
	private double factor;

	/** The image gallery height. */
	private double imageGalleryWidth, imageGalleryHeight;

	/** The image gallery. */
	private Image imageGallery;

	/** The main wrapper. */
	final AnchorPane mainWrapper;

	/** The actual image index. */
	private int actualImageIndex = 0;

	/** The percentage activation. */
	private double percentageActivation = 0.5;

	/** The canvas. */
	private final Canvas canvas;

	/** The photos. */
	private final List<PhotoGallery> photos;

	/** The close. */
	private final Label left, right, close;

	/** The loading. */
	private final Label loading;

	/** The counter label. */
	private Label counterLabel;

	/** The thumbnails manager. */
	private ThumbnailsManager thumbnailsManager;

	/** The semaphore. */
	final Semaphore semaphore = new Semaphore(1);

	/** The gallery controls. */
	private final EventHandler<KeyEvent> galleryControls;

	/**
	 * Instantiates a new image gallery.
	 * 
	 * @param parentStage
	 *            the parent stage
	 * @param photos
	 *            the photos
	 * @param percentagePrevImage
	 *            the percentage prev image
	 */
	public ImageGallery(final Window parentStage, final List<PhotoGallery> photos, final Double percentagePrevImage) {

		// Esta es la ventana principal
		// final VBox vBox = new VBox();
		getStyleClass().add("gallery-modal");

		if (photos == null || photos.isEmpty()) {
			throw new IllegalArgumentException("Empty or null urls");
		}

		this.photos = photos;

		if (percentagePrevImage != null) {
			percentageActivation = percentagePrevImage;
		}

		this.parentStage = parentStage;

		ChangeListener<Number> resizeListener = (v, o, n) -> {
			syncParentStage(parentStage);
			redraw();
		};

		parentStage.heightProperty().addListener(resizeListener);
		parentStage.widthProperty().addListener(resizeListener);

		parentStage.setOnCloseRequest(e -> {
			((StackPane) parentStage.getScene().getRoot()).getChildren().remove(ImageGallery.this);
		});

		// Wrapper para poner los botones encima de la foto
		mainWrapper = new AnchorPane();

		// HBox para centrar el canvas horizontalmente
		final HBox main = new HBox();
		AnchorPane.setBottomAnchor(main, 0D);
		AnchorPane.setTopAnchor(main, 0D);
		AnchorPane.setLeftAnchor(main, 0D);
		AnchorPane.setRightAnchor(main, 0D);

		setOnMouseClicked(e -> {
			if (main.equals(e.getTarget())) {
				((StackPane) parentStage.getScene().getRoot()).getChildren().remove(ImageGallery.this);
			}
		});

		main.setAlignment(Pos.CENTER);

		canvas = new Canvas();

		left = new Label();
		left.setVisible(false);
		left.getStyleClass().add("left");

		right = new Label();
		right.setVisible(false);
		right.getStyleClass().add("right");

		close = new Label();
		close.getStyleClass().add("close");
		close.setPrefSize(19, 19);
		close.setVisible(false);

		final Canvas tooltip = new Canvas();
		tooltip.setWidth(100);
		tooltip.setHeight(40);
		tooltip.setVisible(false);
		final GraphicsContext gc = tooltip.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(5, 5, 60, 22);
		final int inicio = 46;
		gc.fillPolygon(new double[] { inicio + 0, inicio + 5, inicio + 10 }, new double[] { 5, 0, 5 }, 3);
		gc.setFill(Color.WHITE);
		gc.fillText("Cerrar", 15, 21);

		close.setOnMouseEntered(e -> tooltip.setVisible(true));
		close.setOnMouseExited(e -> tooltip.setVisible(false));
		close.setOnMouseClicked(e -> close());

		loading = new Label();
		loading.setPrefSize(30, 30);
		loading.getStyleClass().add("loading");
		loading.setAlignment(Pos.CENTER);
		loading.setGraphic(
				new ImageView(new Image(ImageGallery.class.getResourceAsStream("/image-gallery/loading.gif"))));
		loading.setVisible(false);

		close.layoutXProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> arg0, final Number oldValue,
					final Number newValue) {
				tooltip.setLayoutX(newValue.doubleValue() - 42);
			}
		});
		close.layoutYProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> arg0, final Number oldValue,
					final Number newValue) {
				tooltip.setLayoutY(newValue.doubleValue() + 22);
			}
		});

		canvas.layoutXProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> arg0, final Number oldValue,
					final Number newValue) {
				left.setLayoutX(newValue.doubleValue());
				right.setLayoutX(newValue.doubleValue() + canvas.getWidth() / 2D);
				loading.setLayoutX(newValue.doubleValue() + canvas.getWidth() / 2D - 15);
				close.setLayoutX(newValue.doubleValue() + canvas.getWidth() - 22 - 10);
			}
		});
		canvas.layoutYProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> arg0, final Number oldValue,
					final Number newValue) {
				left.setLayoutY(newValue.doubleValue());
				right.setLayoutY(newValue.doubleValue());
				loading.setLayoutY(newValue.doubleValue() + canvas.getHeight() / 2D - 15);
				close.setLayoutY(newValue.doubleValue() + 10);
			}
		});
		canvas.widthProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> arg0, final Number oldValue,
					final Number newValue) {
				left.setPrefWidth(newValue.doubleValue() / 2D);
				right.setPrefWidth(newValue.doubleValue() / 2D);
			}
		});
		canvas.heightProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> arg0, final Number oldValue,
					final Number newValue) {
				left.setPrefHeight(imageGalleryHeight * factor);
				right.setPrefHeight(imageGalleryHeight * factor);
			}
		});

		main.getChildren().addAll(canvas);
		VBox.setVgrow(mainWrapper, Priority.NEVER);

		mainWrapper.getChildren().addAll(main, left, right, loading, close, tooltip);

		final HBox counterPane = new HBox();
		counterPane.setAlignment(Pos.CENTER_RIGHT);
		VBox.setMargin(counterPane, new Insets(0D, 20D, 0D, 0D));

		counterLabel = new Label();
		counterLabel.getStyleClass().add("counter");
		counterPane.getChildren().add(counterLabel);
		counterPane.setMinHeight(COUNTER_HEIGHT);
		counterPane.setMaxHeight(COUNTER_HEIGHT);

		thumbnailsManager = new ThumbnailsManager(photos, photo -> {

			actualImageIndex = photos.indexOf(photo);
			updateImage(photo);
			return null;
		});
		thumbnailsManager.setMinHeight(THUMBNAILS_HEIGHT);
		thumbnailsManager.setMaxHeight(THUMBNAILS_HEIGHT);

		VBox.setMargin(thumbnailsManager, new Insets(0D));
		VBox.setVgrow(thumbnailsManager, Priority.NEVER);

		getChildren().addAll(mainWrapper, counterPane, thumbnailsManager);

		parentStage.getScene().getStylesheets()
				.add(getClass().getResource("/image-gallery/gallery.css").toExternalForm());
		((StackPane) parentStage.getScene().getRoot()).getChildren().add(this);

		final EventHandler<MouseEvent> mouseEventHandler = event -> {
			if (!left.contains(event.getX(), event.getY()) && !right.contains(event.getX(), event.getY())) {
				left.setVisible(false);
				right.setVisible(false);
			}
		};

		left.setOnMouseExited(mouseEventHandler);
		right.setOnMouseExited(mouseEventHandler);

		canvas.setOnMouseEntered(e -> {
			left.setVisible(true);
			right.setVisible(true);
		});

		left.setOnMouseClicked(e -> prevImage());
		right.setOnMouseClicked(e -> nextImage());

		galleryControls = keyEvent -> {
			if (keyEvent.getCode() == KeyCode.LEFT) {
				prevImage();
			} else if (keyEvent.getCode() == KeyCode.RIGHT) {
				nextImage();
			} else if (keyEvent.getCode() == KeyCode.ESCAPE) {
				close();
			}
		};

		// Añadir al crear
		parentStage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, galleryControls);

	}

	/**
	 * Open the gallery and show the first photo.
	 */
	public void show() {
		show(0);
	}

	/**
	 * Open the gallery and show the photo of the position index.
	 * 
	 * @param index
	 *            the index of the photo in the List photos.
	 */
	public void show(final int index) {

		threadPool = Executors.newSingleThreadExecutor();

		actualImageIndex = index;

		counterLabel.setText(index + 1 + "/" + photos.size());

		updateImage(photos.get(actualImageIndex));

		redraw();

		syncParentStage(parentStage);
	}

	/**
	 * Close the gallery modal.
	 */
	public void close() {

		((StackPane) parentStage.getScene().getRoot()).getChildren().remove(this);
		parentStage.getScene().removeEventHandler(KeyEvent.KEY_RELEASED, galleryControls);
		threadPool.shutdown();
	}

	/**
	 * Next image.
	 */
	private final void nextImage() {
		updateImage(photos.get(getNextImage()));
	}

	/**
	 * Prev image.
	 */
	private final void prevImage() {
		updateImage(photos.get(getPrevImage()));
	}

	/**
	 * Update image.
	 * 
	 * @param photo
	 *            the photo
	 */
	private final void updateImage(final PhotoGallery photo) {

		final int position = photos.indexOf(photo) + 1;

		counterLabel.setText(position + "/" + ImageGallery.this.photos.size());

		threadPool.submit(() -> {

			try {
				semaphore.acquire();
			} catch (final InterruptedException e) {
			}
			Platform.runLater(() -> refreshImage2(photo, position));

		});

	}

	/**
	 * Refresh image2.
	 * 
	 * @param photo
	 *            the photo
	 * @param position
	 *            the position
	 */
	private final void refreshImage2(final PhotoGallery photo, final int position) {

		loading.setVisible(true);

		threadPool.submit(() -> {

			final byte[] image = getImage(photo.getPhotoUrl());

			if (image != null) {
				Platform.runLater(() -> {
					thumbnailsManager.updateThumbnail(photo.getThumbnailUrl(), position);

					imageGallery = new Image(new ByteArrayInputStream(image));
					imageGalleryWidth = imageGallery.getWidth();
					imageGalleryHeight = imageGallery.getHeight();

					doCalculations();

					loading.setVisible(false);

					redraw();

					semaphore.release();
				});
			}
		});

	}

	/**
	 * Redraw.
	 */
	private void redraw() {

		final double height = imageGalleryHeight * factor;

		if (imageGallery != null) {

			canvas.getGraphicsContext2D().drawImage(imageGallery, 0, 0, imageGalleryWidth, imageGalleryHeight, 0, 0,
					canvasWidth, height);

		}

		canvas.getGraphicsContext2D().setStroke(new Color(1, 1, 1, 0.3));
		canvas.getGraphicsContext2D().setLineWidth(1);
		canvas.getGraphicsContext2D().strokeRect(1, 1, canvasWidth - 2, height - 2);
		// canvas.getGraphicsContext2D().strokeRect(0, 0, canvasWidth, height);

		close.setVisible(true);

	}

	/**
	 * Gets the prev image.
	 * 
	 * @return the prev image
	 */
	private int getPrevImage() {

		if (--actualImageIndex < 0) {
			actualImageIndex = photos.size() - 1;
		}
		return actualImageIndex;

	}

	/**
	 * Gets the next image.
	 * 
	 * @return the next image
	 */
	private int getNextImage() {

		if (++actualImageIndex >= photos.size()) {
			actualImageIndex = 0;
		}
		return actualImageIndex;

	}

	/**
	 * Do calculations.
	 */
	private final void doCalculations() {

		final double parentHeight = mainWrapper.getHeight();
		final double parentWidth = mainWrapper.getWidth();

		Double height = Math.min(parentHeight * 0.9, imageGalleryHeight);

		factor = height / imageGalleryHeight;
		canvasWidth = (int) (imageGalleryWidth * factor);

		if (canvasWidth > parentWidth) {
			factor = parentWidth / imageGalleryWidth;
			canvasWidth = (int) (imageGalleryWidth * factor);
			height = imageGalleryHeight * factor;
		}

		canvas.setWidth(canvasWidth);
		canvas.setHeight(height);

	}

	/**
	 * Sync parent stage.
	 * 
	 * @param parentStage
	 *            the parent stage
	 */
	private final void syncParentStage(final Window parentStage) {

		setMinWidth(parentStage.getScene().getWidth());
		setMaxWidth(parentStage.getScene().getWidth());
		setMinHeight(parentStage.getScene().getHeight());
		setMaxHeight(parentStage.getScene().getHeight());

		mainWrapper.setMaxWidth(parentStage.getScene().getWidth());
		mainWrapper.setMinWidth(parentStage.getScene().getWidth());
		mainWrapper.setMinHeight(getMaxHeight() - THUMBNAILS_HEIGHT - COUNTER_HEIGHT);
		mainWrapper.setMaxHeight(getMaxHeight() - THUMBNAILS_HEIGHT - COUNTER_HEIGHT);

		thumbnailsManager.setMaxWidth(parentStage.getScene().getWidth());
		thumbnailsManager.setMinWidth(parentStage.getScene().getWidth());

		doCalculations();
	}

	/**
	 * To byte array.
	 * 
	 * @param inputStream
	 *            input stream
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	static final byte[] toByteArray(final InputStream inputStream) throws IOException {

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		final byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = inputStream.read(buffer))) {
			outputStream.write(buffer, 0, n);
		}

		return outputStream.toByteArray();

	}

	public static byte[] getImage(final String imageUrl) {

		byte[] image = null;
		InputStream inputStream;

		if (imageUrl.startsWith("http")) {
			URL url;
			HttpURLConnection connection;

			try {
				url = new URL(imageUrl);
				connection = (HttpURLConnection) url.openConnection();

				inputStream = connection.getInputStream();
				image = toByteArray(inputStream);
				inputStream.close();
				connection.disconnect();
			} catch (final Exception e) {
				LOGGER.error("Error obteniendo el inputStream a partir de una URL", e);
			}

		} else {

			try {
				inputStream = new FileInputStream(imageUrl);
				image = toByteArray(inputStream);
				inputStream.close();
			} catch (final IOException e) {
				LOGGER.error("Error obteniendo el inputStream a partir de un file", e);
			}

		}

		return image;

	}

}
