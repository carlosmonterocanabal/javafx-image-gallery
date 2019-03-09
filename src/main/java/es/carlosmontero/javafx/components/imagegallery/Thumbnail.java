package es.carlosmontero.javafx.components.imagegallery;

import java.io.ByteArrayInputStream;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

class Thumbnail extends StackPane {

	private final Double size = 50D;
	private final Double imageSize = 100D;

	private final String urlToImage;
	private final Label imageLabel;
	private boolean loaded = false;

	public Thumbnail(final String urlToImage) {
		super();

		this.urlToImage = urlToImage;

		this.setAlignment(Pos.CENTER);

		final ImageView loading = new ImageView(
				new Image(Thumbnail.class.getResourceAsStream("/image-gallery/loading.gif")));

		imageLabel = new Label();

		imageLabel.setGraphic(loading);
		imageLabel.setAlignment(Pos.CENTER);
		setMinSize(size, size);
		setMaxSize(size, size);

		imageLabel.setMinSize(size - 2, size - 2);
		imageLabel.setMaxSize(size - 2, size - 2);

		final Rectangle rectangle = new Rectangle();
		rectangle.setWidth(size - 2);
		rectangle.setHeight(size - 2);
		imageLabel.setClip(rectangle);

		final Rectangle rectangle2 = new Rectangle();
		rectangle2.setWidth(50);
		rectangle2.setHeight(50);
		setClip(rectangle2);

		getChildren().add(imageLabel);

		getStyleClass().add("thumbnail");

	}

	public void init() {
		if (!loaded) {
			loaded = true;
			ThumbnailsManager.threadPool.submit(() -> {

				final byte[] image = ImageGallery.getImage(urlToImage);

				Platform.runLater(() -> {

					final Image thumbnail = new Image(new ByteArrayInputStream(image));

					final ImageView iv = new ImageView(thumbnail);
					iv.setFitHeight(imageSize);
					iv.setFitWidth(imageSize);
					iv.setSmooth(true);
					iv.setPreserveRatio(true);

					imageLabel.setGraphic(iv);
					Thumbnail.this.getStyleClass().add("loaded");

				});
			});
		}
	}

}
