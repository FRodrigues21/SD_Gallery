package sd.tp1.clt;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import sd.tp1.gui.GalleryContentProvider;

import sd.tp1.gui.Gui;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryContentProvider implements GalleryContentProvider {

	Gui gui;

	SharedGalleryServerDiscovery discovery;

	private long REFRESH_TIME = 5000;
	private long RETRY_TIME = 5000;
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

	public void findServers() throws IOException {
		if(discovery == null) {
			discovery = new SharedGalleryServerDiscovery();
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
				findServers();
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
		boolean executed;
		current_album = null;
		List<String> lst = new ArrayList<>();
		for (Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; i < MAX_RETRIES && !executed; i++) {
				List<String> tmp;
				try {
					tmp = e.getListOfAlbums();
					if (tmp != null) {
						lst.removeAll(tmp);
						lst.addAll(tmp);
					}
					executed = true;
				}
				catch (RuntimeException ex) {
					if (e.getTries() == MAX_RETRIES + 1)
						discovery.removeServer(e.getAddress());
					try {
						Thread.sleep(RETRY_TIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
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
		boolean executed;
		current_album = album;
		List<String> lst = new ArrayList<>();
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				List<String> tmp;
				try {
					tmp = e.getListOfPictures(album);
					if (tmp != null) {
						lst.removeAll(tmp);
						lst.addAll(tmp);
					}
					executed = true;
				} catch (RuntimeException ex) {
					if (e.getTries() == MAX_RETRIES + 1)
						discovery.removeServer(e.getAddress());
					try {
						Thread.sleep(RETRY_TIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
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
		boolean executed;

		if(cache != null && cache.containsKey(album.getName() + "_" + picture.getName())) {
			return cache.get(album.getName() + "_" + picture.getName());
		}
		else {
			for(Request e : discovery.getServers().values()) {
				executed = false;
				for(int i = 0; !executed && i < MAX_RETRIES; i++) {
					try {
						data = e.getPictureData(album, picture);
						if (data != null && data.length > 1) {
							cache.put(album.getName() + '_' + picture.getName(), data);
							return data;
						}
						executed = true;
					} catch (RuntimeException ex) {
						if (e.getTries() == MAX_RETRIES + 1)
							discovery.removeServer(e.getAddress());
						try {
							Thread.sleep(RETRY_TIME);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
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
		if(current_albumlist.contains(name))
			return null;
		boolean executed = false;
		int server = (int)(Math.random() * discovery.getServers().size());
		Request request = new ArrayList<>(discovery.getServers().values()).get(server);
		for(int i = 0; !executed && i < MAX_RETRIES; i++) {
			try {
				Album album = new SharedAlbum(request.createAlbum(name));
				if (album != null)
					return album;
				executed = true;
			} catch (RuntimeException ex) {
				if (request.getTries() == MAX_RETRIES + 1)
					discovery.removeServer(request.getAddress());
				try {
					Thread.sleep(RETRY_TIME);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Delete an existing album.
	*/
	@Override
	public void deleteAlbum(Album album) {
		boolean executed;
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					e.deleteAlbum(album);
					executed = true;
				} catch (RuntimeException ex) {
					if (e.getTries() == MAX_RETRIES + 1)
						discovery.removeServer(e.getAddress());
					try {
						Thread.sleep(RETRY_TIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Add a new picture to an album.
	 * On error this method should return null.
	*/
	@Override
	public Picture uploadPicture(Album album, String name, byte [] data) {
		if(current_picturelist.contains(name))
			return null;
		boolean executed;
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					Picture picture = new SharedPicture(e.uploadPicture(album, name, data));
					if (picture.getName().equalsIgnoreCase(name)) {
						return picture;
					}
					executed = true;
				} catch (RuntimeException ex) {
					if (e.getTries() == MAX_RETRIES+1)
						discovery.removeServer(e.getAddress());
					try {
						Thread.sleep(RETRY_TIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
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
		boolean executed;
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					if(e.deletePicture(album, picture)) {
						return true;
					}
					executed = true;
				}
				catch (RuntimeException ex) {
					if(e.getTries() == MAX_RETRIES+1)
						discovery.removeServer(e.getAddress());
					try {
						Thread.sleep(RETRY_TIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	private void detectChanges() {
		Thread t = new Thread(() -> {
            while(true) {
                try {
                    List<String> lst_current;
                    List<String> lst_possible;

                    lst_current = current_albumlist;
                    lst_possible = getListOfAlbums().stream().map(f -> f.getName()).collect(Collectors.toList());
                    if(!listsAreEqual(lst_current, lst_possible)) {
                        current_albumlist = lst_possible;
                        gui.updateAlbums();
                    }

                    if(current_album != null) {
                        lst_current = current_picturelist;
                        lst_possible = getListOfPictures(current_album).stream().map(f -> f.getName()).collect(Collectors.toList());
                        if(!listsAreEqual(lst_current, lst_possible)) {
                            current_picturelist = lst_possible;
                            gui.updateAlbum(current_album);
                        }
                    }

                    Thread.sleep(REFRESH_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
		t.start();
	}

	private Boolean listsAreEqual(List<String> lst1, List<String> lst2) {
		Collections.sort(lst1);
		Collections.sort(lst2);
		return !(lst1.size() != lst2.size() || !lst1.equals(lst2));
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
