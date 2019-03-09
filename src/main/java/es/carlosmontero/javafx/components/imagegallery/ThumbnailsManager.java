package es.carlosmontero.javafx.components.imagegallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

class ThumbnailsManager extends HBox {

	static ExecutorService threadPool = Executors.newCachedThreadPool();

	private int elementsPerPage = 0;
	private int actualPage = 1;
	private final Map<String, Thumbnail> thumbnailsByUrl = new HashMap<String, Thumbnail>();
	private final List<PhotoGallery> urls;
	private final HBox thumbnails;

	private Thumbnail active;
	private final Label left, right;

	public ThumbnailsManager(final List<PhotoGallery> urls, final Callback<PhotoGallery, Void> callback) {

		super();

		this.urls = new ArrayList<PhotoGallery>(urls);

		thumbnails = new HBox();
		thumbnails.setAlignment(Pos.CENTER_LEFT);
		thumbnails.setSpacing(10);

		left = new Label("<");
		right = new Label(">");
		left.getStyleClass().add("navigation");
		right.getStyleClass().add("navigation");

		left.setOnMouseClicked(e -> {
			if (actualPage > 1) {
				goToPage(actualPage - 1);
				actualPage--;
			}
		});

		right.setOnMouseClicked(e -> {
			if (elementsPerPage * actualPage < urls.size()) {
				goToPage(actualPage + 1);
				actualPage++;
			}
		});

		getChildren().addAll(left, thumbnails, right);

		getStyleClass().add("thumbnails");
		setAlignment(Pos.CENTER);
		setPadding(new Insets(10));
		setSpacing(20);

		final double buttonsWidth = 2 * 50 + 2 * 10;

		widthProperty().addListener((value, oldValue, newValue) -> {
			goToPage(1);
			actualPage = 1;
			thumbnails.setMinWidth(-1);

			calculateThumbnails(buttonsWidth, oldValue, newValue);
		});

		for (final PhotoGallery url : urls) {
			final Thumbnail thumbnail = new Thumbnail(url.getThumbnailUrl());

			thumbnailsByUrl.put(url.getThumbnailUrl(), thumbnail);

			thumbnail.setOnMouseClicked(e -> {
				if (active != null && thumbnailsByUrl.get(url.getThumbnailUrl()).equals(active)) {
					return;
				}

				callback.call(url);
			});
		}

	}

	private void calculateThumbnails(final Double buttonsWidth, final Number oldValue, final Number newValue) {

		final boolean isFirstTime = oldValue.doubleValue() == 0;

		if (oldValue.doubleValue() > newValue.doubleValue()) {

			// Se acorta el tamano

			if (newValue.doubleValue() < elementsPerPage * (50 + 10) + buttonsWidth) {
				thumbnails.getChildren().remove(elementsPerPage - 1);
				elementsPerPage--;

			}

		} else if (isFirstTime || oldValue.doubleValue() < newValue.doubleValue()) {

			// Se agranda el tamano
			boolean change = false;

			if (newValue.doubleValue() > (elementsPerPage + 1) * (50 + 10) + buttonsWidth) {
				if (elementsPerPage < urls.size()) {
					final Thumbnail thumbnail = thumbnailsByUrl.get(urls.get(elementsPerPage++).getThumbnailUrl());
					thumbnails.getChildren().add(thumbnail);
					if (isFirstTime) {
						thumbnail.init();
					}

					change = true;
				}
			}

			if (isFirstTime && change) {
				calculateThumbnails(buttonsWidth, oldValue, newValue);
			}

		}

	}

	private void goToPage(final int page) {

		thumbnails.setMinWidth(thumbnails.getWidth());

		thumbnails.getChildren().clear();
		for (int i = elementsPerPage * (page - 1); i < elementsPerPage * page && i < thumbnailsByUrl.size(); i++) {
			final Thumbnail thumbnail = thumbnailsByUrl.get(urls.get(i).getThumbnailUrl());
			thumbnails.getChildren().add(thumbnail);
			thumbnail.init();
		}

		if (page == 1) {
			if (left.getStyleClass().remove("navigation")) {
				left.getStyleClass().add("navigation_disable");
			}
		} else {
			if (left.getStyleClass().remove("navigation_disable")) {
				left.getStyleClass().add("navigation");
			}
		}

		if (elementsPerPage * page >= thumbnailsByUrl.size()) {
			if (right.getStyleClass().remove("navigation")) {
				right.getStyleClass().add("navigation_disable");
			}
		} else {
			if (right.getStyleClass().remove("navigation_disable")) {
				right.getStyleClass().add("navigation");
			}
		}

	}

	public void updateThumbnail(final String url, final int position) {

		if (active != null) {
			active.getStyleClass().remove("active");
		}
		active = thumbnailsByUrl.get(url);
		active.getStyleClass().add("active");

		final int max = actualPage * elementsPerPage;
		final int min = (actualPage - 1) * elementsPerPage;

		if (!(position > min && position <= max)) {

			if (position > max) {

				if (position > (actualPage + 1) * elementsPerPage) {

					final int lastPage = position / elementsPerPage + (position % elementsPerPage == 0 ? 0 : 1);
					goToPage(lastPage);
					actualPage = lastPage;

				} else {
					goToPage(actualPage + 1);
					actualPage++;
				}
			} else if (position <= min) {

				if (position == 1) {
					goToPage(1);
					actualPage = 1;
				} else {
					goToPage(actualPage - 1);
					actualPage--;
				}
			}

		}

	}

	public void destroy() {
		threadPool.shutdown();
	}

}
