package sd.tp1.clt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sd.tp1.gui.GalleryContentProvider;

import sd.tp1.gui.Gui;

/*
 * This class provides the album/picture content to the gui/main application.
 * 
 * Project 1 implementation should complete this class. 
 */
public class SharedGalleryContentProvider implements GalleryContentProvider {

	Gui gui;
	ServerDiscovery discovery;
	Album current_album = null;

	SharedGalleryContentProvider() { }

	private void detectChanges() {
		Thread t = new Thread(new Runnable() {
			public void run()
			{
				while(true) {
					try {
						gui.updateAlbums();
						if(current_album != null)
							gui.updateAlbum(current_album);
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		});
		t.start();
	}

	public void findServers(Gui gui) throws IOException {
		if(discovery == null) {
			discovery = new ServerDiscovery(gui);
			new Thread(discovery).start();
		}
	}

	/**
	 *  Downcall from the GUI to register itself, so that it can be updated via upcalls.
	 */
	@Override
	public void register(Gui gui) {
		if(this.gui == null) {
			this.gui = gui;
			try {
				findServers(this.gui);
				detectChanges();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the list of albums in the system.
	 * On error this method should return null.
	 */
	@Override
	public List<Album> getListOfAlbums() {
		List<Album> lst = new ArrayList<>();
		for(Request e : discovery.getServers().values()) {
            List<Album> tmp = e.getListOfAlbums();
            if(tmp != null)
			    lst.addAll(tmp);
		}
		return lst;
	}

	/**
	 * Returns the list of pictures for the given album. 
	 * On error this method should return null.
	 */
	@Override
	public List<Picture> getListOfPictures(Album album) {
		current_album = album;
		List<Picture> lst = new ArrayList<>();
		for(Request e : discovery.getServers().values()) {
			List<Picture> tmp = e.getListOfPictures(album);
			if(tmp != null)
				lst.addAll(tmp);
		}
		return lst;
	}

	/**
	 * Returns the contents of picture in album.
	 * On error this method should return null.
	 */
	@Override
	public byte[] getPictureData(Album album, Picture picture) {
		byte [] data;
		for(Request e : discovery.getServers().values()) {
			data = e.getPictureData(album, picture);
			if(data != null)
				return data;
		}
		return null;
	}

	/*
	 * Create a new album.
	 * On error this method should return null.
	 */

	@Override
	public Album createAlbum(String name) {
		int server = (int)(Math.random() * discovery.getServers().size());
		int cnt = 0;
		for (Request e : discovery.getServers().values()) {
			if(cnt == server) {
				Album album = e.createAlbum(name);
				if(album.getName().equalsIgnoreCase(name))
					return album;
			}
			cnt++;
		}
		return null;
	}

	/**
	 * Delete an existing album.
	*/
	@Override
	public void deleteAlbum(Album album) {
		for(Request e : discovery.getServers().values()) {
			e.deleteAlbum(album);
		}
	}
	
	/**
	 * Add a new picture to an album.
	 * On error this method should return null.
	*/
	@Override
	public Picture uploadPicture(Album album, String name, byte [] data) {
		int server = (int)(Math.random() * discovery.getServers().size());
		int cnt = 0;
		for (Request e : discovery.getServers().values()) {
			if(cnt == server) {
				Picture picture = e.uploadPicture(album, name, data);
				if(picture.getName().equalsIgnoreCase(name))
					return picture;
			}
			cnt++;
		}
		return null;
	}

	/**
	 * Delete a picture from an album.
	 * On error this method should return false.
	 */
	@Override
	public boolean deletePicture(Album album, Picture picture) {
		for(Request e : discovery.getServers().values()) {
			if(e.deletePicture(album, picture))
				return true;
		}
		return false;
	}

}
