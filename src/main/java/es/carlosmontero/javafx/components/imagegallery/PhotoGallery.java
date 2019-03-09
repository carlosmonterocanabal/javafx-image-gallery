package es.carlosmontero.javafx.components.imagegallery;

/**
 * The Class PhotoGallery. A Utility class for the main Gallery class.
 * 
 * @author Carlos Montero Canabal
 * 
 */
public class PhotoGallery {

	/** The thumbnail url. */
	private String thumbnailUrl;

	/** The photo url. */
	private String photoUrl;

	/**
	 * Instantiates a new photo gallery. If you indicate a unique URL, the
	 * thumbnail and the main photo will be the same.
	 * 
	 * @param photoUrl
	 *            the photo url
	 */
	public PhotoGallery(String photoUrl) {
		super();
		this.thumbnailUrl = photoUrl;
		this.photoUrl = photoUrl;
	}

	/**
	 * Instantiates a new photo gallery. You can indicate a URL for the
	 * thumbnail photo and a URL for the main photo. You can optimize the
	 * loading if the thumbnail photo is smaller than the main photo.
	 * 
	 * @param thumbnailUrl
	 *            the thumbnail url
	 * @param photoUrl
	 *            the photo url
	 */
	public PhotoGallery(String thumbnailUrl, String photoUrl) {
		super();
		this.thumbnailUrl = thumbnailUrl;
		this.photoUrl = photoUrl;
	}

	/**
	 * Gets the thumbnail url.
	 * 
	 * @return the thumbnail url
	 */
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	/**
	 * Gets the photo url.
	 * 
	 * @return the photo url
	 */
	public String getPhotoUrl() {
		return photoUrl;
	}

}
