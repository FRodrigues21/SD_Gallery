package sd.tp1.clt;

import java.io.IOException;
import java.security.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
	private int MAX_CACHE_CAPACITY = 8; // In number of entries
	private int MAX_CACHE_TIME = 2; // In minutes
	private int MAX_RETRIES = 3;
	private SharedGalleryContentCache<String, SharedPicture> cache;

	private Album current_album = null;
	private List<String> current_picturelist = null;
	private List<String> current_albumlist = null;

	SharedGalleryContentProvider() {
		cache = new SharedGalleryContentCache<>(MAX_CACHE_CAPACITY);
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
		if(discovery.getServers().isEmpty())
			return null;
		current_album = album;
		boolean executed;
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

		if(discovery.getServers().isEmpty())
			return null;

		String key = album.getName() + "_" + picture.getName();
		if(cache.containsKey(key) && timeBetween(cache.get(key).getCreation(), System.currentTimeMillis()) < MAX_CACHE_TIME) {
			return cache.get(album.getName() + "_" + picture.getName()).getData();
		}
		else {
			for(Request e : discovery.getServers().values()) {
				executed = false;
				for(int i = 0; !executed && i < MAX_RETRIES; i++) {
					try {
						data = e.getPictureData(album, picture);
						if (data != null && data.length > 1) {
							SharedPicture cached_picture = new SharedPicture(picture.getName());
							cached_picture.setData(data);
							cache.put(album.getName() + '_' + picture.getName(), cached_picture);
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
		if(current_albumlist.contains(name) || name.isEmpty() || discovery.getServers().isEmpty())
			return null;
		boolean executed = false;
		int server = (int)(Math.random() * discovery.getServers().size());
		Request request = new ArrayList<>(discovery.getServers().values()).get(server);
		System.out.println(request.getAddress());
		for(int i = 0; !executed && i < MAX_RETRIES; i++) {
			try {
				String received = request.createAlbum(name);
				if (received != null && received.equalsIgnoreCase(name))
				{
					SharedAlbum album = new SharedAlbum(name);
					current_albumlist.add(album.getName());
					return album;
				}
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
		if(current_album != null && current_album.getName().equalsIgnoreCase(album.getName()))
			current_album = null;
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					if(e.deleteAlbum(album) && current_albumlist.contains(album.getName()))
						current_albumlist.remove(album.getName());
					executed = true;
				} catch (RuntimeException e1) {
					if (e.getTries() == MAX_RETRIES + 1)
						discovery.removeServer(e.getAddress());
					try {
						Thread.sleep(RETRY_TIME);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
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
						current_picturelist.add(picture.getName());
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
						current_picturelist.remove(picture.getName());
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

					// Viewing albums so try to update albums
					lst_current = current_albumlist;
					lst_possible = getListOfAlbums().stream().map(f -> f.getName()).collect(Collectors.toList());
					if(!listsAreEqual(lst_current, lst_possible)) {
						current_albumlist = lst_possible;
						gui.updateAlbums();
					}

					// Update last album viewed
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
		if(lst1 == null | lst2 == null)
			return false;
		Collections.sort(lst1);
		Collections.sort(lst2);
		return !(lst1.size() != lst2.size() || !lst1.equals(lst2));
	}

	private double timeBetween(long time1, long time2) {
		long result = time2 - time1;
		return TimeUnit.MILLISECONDS.toMinutes(result);
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
		final Long created;
		byte [] data;

		SharedPicture(String name) {
			this.name = name;
			this.created = System.currentTimeMillis();
		}

		@Override
		public String getName() {
			return name;
		}

		// Data of the picture
		public byte [] getData() { return data; }

		// Set of picture
		public void setData(byte [] data) { this.data = data; }

		// Time of creation
		public long getCreation() { return created; }

	}

}
