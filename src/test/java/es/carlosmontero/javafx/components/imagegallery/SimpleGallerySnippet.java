package es.carlosmontero.javafx.components.imagegallery;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SimpleGallerySnippet extends Application {

	@Override
	public void start(final Stage stage) throws Exception {

		final String[] urls = new String[] { "http://carlosmontero.es/photoprovider/portfolio/2/false/image/15",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/16",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/17",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/18",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/19",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/20",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/21",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/22",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/23",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/24",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/25",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/26",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/27",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/28",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/29",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/30",
				"http://carlosmontero.es/photoprovider/portfolio/2/false/image/31" };

		final HBox hBox = new HBox();
		hBox.setStyle("-fx-background-color: grey");
		hBox.setAlignment(Pos.CENTER);

		final List<PhotoGallery> photos = new ArrayList<PhotoGallery>();
		for (String url : urls) {
			photos.add(new PhotoGallery(url));
		}

		final Button button = new Button("Abrir galería");
		button.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent arg0) {
				final ImageGallery gallery = new ImageGallery(stage, photos, 0.5);
				gallery.show();
			}

		});

		hBox.getChildren().addAll(button);

		final StackPane root = new StackPane();
		root.getChildren().add(hBox);

		final Scene scene = new Scene(root);
		stage.setScene(scene);

		stage.setWidth(800);
		stage.setHeight(700);
		stage.setTitle("Visor de imágenes en JavaFX");
		stage.show();

	}

	public static void main(final String[] args) {
		launch(args);
	}

}
