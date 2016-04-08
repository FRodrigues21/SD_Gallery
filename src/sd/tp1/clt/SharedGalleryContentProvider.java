package sd.tp1.clt;

import java.io.IOException;
import java.util.*;
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

	private int MAX_CACHE = 5;
	private int MAX_RETRIES = 3;
	private SharedGalleryContentCache<String, byte []> cache;

	private Album current_album = null;
	private List<String> current_picturelist = null;
	private List<String> current_albumlist = null;

	SharedGalleryContentProvider() {
		cache = new SharedGalleryContentCache<>(MAX_CACHE);
		current_albumlist = new ArrayList<>();
		current_picturelist = new ArrayList<>();
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
		System.err.println("CLIENT WARNING: RETRIEVING ALBUM LIST FROM SERVER!");
		for (Request e : discovery.getServers().values()) {
			List<String> tmp;
			try {
				tmp = e.getListOfAlbums();
				if (tmp != null) {
					lst.removeAll(tmp);
					lst.addAll(tmp);
				}
			}
			catch (RuntimeException ex) {
				System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
				if (e.getTries() == MAX_RETRIES)
					discovery.removeServer(e.getAddress());
			}
		}
		current_albumlist = lst;
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
			List<String> tmp;
			try {
				tmp = e.getListOfPictures(album);
				if(tmp != null) {
					lst.removeAll(tmp);
					lst.addAll(tmp);
				}
			}
			catch (RuntimeException ex) {
				System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
				if(e.getTries() == MAX_RETRIES)
					discovery.removeServer(e.getAddress());
			}
		}
		current_picturelist = lst;
		return lst.stream().map(s -> new SharedPicture(s)).collect(Collectors.toList());
	}

	/**
	 * Returns the contents of picture in album.
	 * On error this method should return null.
	 */
	@Override
	public byte[] getPictureData(Album album, Picture picture) {
		byte [] data;

		if(cache != null && cache.containsKey(album.getName() + "_" + picture.getName())) {
			System.err.println("CLIENT WARNING: RETRIEVING PICTURE FROM CACHE");
			return cache.get(album.getName() + "_" + picture.getName());
		}

		System.err.println("CLIENT WARNING: PICTURE NOT IN CACHE, RETRIEVING FROM SERVER");
		for(Request e : discovery.getServers().values()) {
			try {
				data = e.getPictureData(album, picture);
				if(data != null && data.length > 1) {
					cache.put(album.getName() + '_' + picture.getName(), data);
					return data;
				}
			}
			catch (RuntimeException ex) {
				System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
				if(e.getTries() == MAX_RETRIES)
					discovery.removeServer(e.getAddress());
			}
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
		Request request = new ArrayList<>(discovery.getServers().values()).get(server);
		try {
			Album album = new SharedAlbum(request.createAlbum(name));
			if(album != null)
				return album;
		}
		catch (RuntimeException ex) {
			System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
			if(request.getTries() == MAX_RETRIES)
				discovery.removeServer(request.getAddress());
		}
		return null;
	}

	/**
	 * Delete an existing album.
	*/
	@Override
	public void deleteAlbum(Album album) {
		for(Request e : discovery.getServers().values()) {
			try {
				e.deleteAlbum(album);
			}
			catch (RuntimeException ex) {
				System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
				if(e.getTries() == MAX_RETRIES)
					discovery.removeServer(e.getAddress());
			}
		}
	}

	/**
	 * Add a new picture to an album.
	 * On error this method should return null.
	*/
	@Override
	public Picture uploadPicture(Album album, String name, byte [] data) {
		for(Request e : discovery.getServers().values()) {
			try {
				Picture picture = new SharedPicture(e.uploadPicture(album, name, data));
				if(picture.getName().equalsIgnoreCase(name)) {
					return picture;
				}
			}
			catch (RuntimeException ex) {
				System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
				if(e.getTries() == MAX_RETRIES)
					discovery.removeServer(e.getAddress());
			}
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
			try {
				if(e.deletePicture(album, picture))
					return true;
			}
			catch (RuntimeException ex) {
				System.err.println("CLIENT ERROR: Couldn't connect to server, trying to remove server from list.");
				if(e.getTries() == MAX_RETRIES)
					discovery.removeServer(e.getAddress());
			}
		}
		return false;
	}

	private void detectChanges() {
		Thread t = new Thread(new Runnable() {
			public void run()
			{
				while(true) {
					try {
						List<String> lst_current;
						List<String> lst_possible;
						// User is viewing the album list
						if(current_album == null) {
							lst_current = current_albumlist;
							lst_possible = getListOfAlbums().stream().map(f -> new String(f.getName())).collect(Collectors.toList());
							if(!listsAreEqual(lst_current, lst_possible)) {
								current_albumlist = lst_possible;
								gui.updateAlbums();
							}
						}
						// User is inside an album
						else {
							lst_current = current_picturelist;
							lst_possible = getListOfPictures(current_album).stream().map(f -> new String(f.getName())).collect(Collectors.toList());
							if(!listsAreEqual(lst_current, lst_possible)) {
								current_picturelist = lst_possible;
								gui.updateAlbum(current_album);
							}
						}
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	private Boolean listsAreEqual(List<String> lst1, List<String> lst2) {
		Collections.sort(lst1);
		Collections.sort(lst2);
		if(lst1.size() != lst2.size() || !lst1.equals(lst2))
			return false;
		else
			return true;
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
