package sd.tp1.clt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sd.tp1.gui.GalleryContentProvider;

import sd.tp1.gui.Gui;

/*
 * This class provides the album/picture content to the gui/main application.
 * 
 * Project 1 implementation should complete this class. 
 */
public class SharedGalleryContentProvider implements GalleryContentProvider {

	Gui gui;
	SharedGalleryServerDiscovery discovery;
	Album current_album = null;

	SharedGalleryContentProvider() { }

	private void detectChanges() {
		Thread t = new Thread(new Runnable() {
			public void run()
			{
				while(true) {
					try {
						// TODO: Check if there are any albums changes
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
			discovery = new SharedGalleryServerDiscovery(gui);
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
		current_album = null;
		List<String> lst = new ArrayList<>();
		for(Request e : discovery.getServers().values()) {
            List<String> tmp = e.getListOfAlbums();
            if(tmp != null) {
				lst.removeAll(tmp);
				lst.addAll(tmp);
			}
		}
		return lst.stream().map(s -> new SharedAlbum(s)).collect(Collectors.toList());
	}

	/**
	 * Returns the list of pictures for the given album. 
	 * On error this method should return null.
	 */
	@Override
	public List<Picture> getListOfPictures(Album album) {
		current_album = album;
		List<String> lst = new ArrayList<>();
		for(Request e : discovery.getServers().values()) {
			List<String> tmp = e.getListOfPictures(album);
			if(tmp != null) {
				lst.removeAll(tmp);
				lst.addAll(tmp);
			}
		}
		return lst.stream().map(s -> new SharedPicture(s)).collect(Collectors.toList());
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
			if(data != null && data.length > 1)
				return data;
		}
		return null;
	}

	/**
	 * Create a new album.
	 * On error this method should return null.
	 */

	@Override
	public Album createAlbum(String name) {
		int server = (int)(Math.random() * discovery.getServers().size());
		int cnt = 0;
		for (Request e : discovery.getServers().values()) {
			if(cnt == server) {
				Album album = new SharedAlbum(e.createAlbum(name));
				if(album != null)
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
				Picture picture = new SharedPicture(e.uploadPicture(album, name, data));
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

	/**
	 * Represents a shared album.
	 */
	static class SharedAlbum implements GalleryContentProvider.Album {
		final String name;

		SharedAlbum(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

	/**
	 * Represents a shared picture.
	 */
	static class SharedPicture implements GalleryContentProvider.Picture {
		final String name;

		SharedPicture(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

}
