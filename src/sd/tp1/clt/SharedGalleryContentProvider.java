package sd.tp1.clt;

import java.io.IOException;
import java.util.List;

import sd.tp1.gui.GalleryContentProvider;

import sd.tp1.gui.Gui;

/*
 * This class provides the album/picture content to the gui/main application.
 * 
 * Project 1 implementation should complete this class. 
 */
public class SharedGalleryContentProvider implements GalleryContentProvider{

	Gui gui;
	ClientSOAP client;

	SharedGalleryContentProvider() throws IOException {
		client = new ClientSOAP();
	}

	/**
	 *  Downcall from the GUI to register itself, so that it can be updated via upcalls.
	 */
	@Override
	public void register(Gui gui) {
		if( this.gui == null ) {
			this.gui = gui;
		}
	}

	/**
	 * Returns the list of albums in the system.
	 * On error this method should return null.
	 */
	@Override
	public List<Album> getListOfAlbums() {
		return client.getListOfAlbums();
	}

	/**
	 * Returns the list of pictures for the given album. 
	 * On error this method should return null.
	 */
	@Override
	public List<Picture> getListOfPictures(Album album) {
		return client.getListOfPictures(album);
	}

	/**
	 * Returns the contents of picture in album.
	 * On error this method should return null.
	 */
	@Override
	public byte[] getPictureData(Album album, Picture picture) {
		return client.getPictureData(album, picture);
	}

	/**
	 * Create a new album.
	 * On error this method should return null.
	 */
	@Override
	public Album createAlbum(String name) {
		return client.createAlbum(name);
	}

	/**
	 * Delete an existing album.
	 */
	@Override
	public void deleteAlbum(Album album) {
		client.deleteAlbum(album);
	}
	
	/**
	 * Add a new picture to an album.
	 * On error this method should return null.
	 */
	@Override
	public Picture uploadPicture(Album album, String name, byte [] data) {
		return client.uploadPicture(album, name, data);
	}

	/**
	 * Delete a picture from an album.
	 * On error this method should return false.
	 */
	@Override
	public boolean deletePicture(Album album, Picture picture) {
		client.deletePicture(album, picture);
		return true;
	}

}
