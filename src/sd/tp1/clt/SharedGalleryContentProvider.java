package sd.tp1.clt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import sd.tp1.gui.GalleryContentProvider;

import sd.tp1.gui.Gui;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryContentProvider implements GalleryContentProvider {

	private Gui gui;

	private SharedGalleryServerDiscovery discovery;

	private static final long RETRY_TIME = 2000; // Time between trying to run the method again
	private static final int MAX_RETRIES = 3; // Max number of retries before deleting the server from the server list

	private static final int MAX_CACHE_CAPACITY = 8; // Cache maximum number of entries
	private static final int MAX_CACHE_TIME = 2; // Cache maximum time in minutes

	private SharedGalleryContentCache<String, SharedPicture> cache;
	private Map<String, List<String>> current_data;

	private String local_password;

	private Properties props;
	private KafkaConsumer<String, String> consumer;
	private List<String> current_topicList;

	SharedGalleryContentProvider(String password) {

		local_password = password;
		try {
			System.out.println("ShareGalleryContentProvider: Started @ " + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			System.err.println("CLIENT ERROR: CLIENT HAS NO ADDRESS! SO IT'S UNREACHABLE");
		}
		cache = new SharedGalleryContentCache<>(MAX_CACHE_CAPACITY);
		current_data = new HashMap<>();
		setupConsumer();
	}

	/**
	 *  Downcall from the GUI to register itself, so that it can be updated via upcalls.
	 */
	@Override
	public void register(Gui gui) {
		if(this.gui == null) {
			this.gui = gui;
			try {
				detectChanges();
				findServers(); // Finds servers
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
		if(current_data.isEmpty())
			return updateAlbums().stream().map(s -> new SharedAlbum(s)).collect(Collectors.toList());
		return current_data.keySet().stream().map(s -> new SharedAlbum(s)).collect(Collectors.toList());
	}

	private List<String> getListOfAlbumsFromServers() {
		List<String> lst = new ArrayList<>();
		boolean executed;
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
		return lst;
	}

	/**
	 * Returns the list of pictures for the given album.
	 * On error this method should return null.
	 */
	@Override
	public List<Picture> getListOfPictures(Album album) {
		if(current_data.get(album.getName()).isEmpty())
			return updateAlbum(album).stream().map(s -> new SharedPicture(s)).collect(Collectors.toList());
		return current_data.get(album.getName()).stream().map(s -> new SharedPicture(s)).collect(Collectors.toList());
	}

	private List<String> getListOfPicturesFromServer(Album album) {
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
		return lst;
	}

	/**
	 * Returns the contents of picture in album.
	 * On error this method should return null.
	 */
	@Override
	public byte[] getPictureData(Album album, Picture picture) {

		System.out.println("[ CLIENT ] Cache current size: " + cache.size());

		byte [] data;
		boolean executed;

		if(discovery.getServers().isEmpty())
			return null;

		String key = album.getName() + "_" + picture.getName();
		if(cache.containsKey(key) && timeBetween(cache.get(key).getCreation(), System.currentTimeMillis()) < MAX_CACHE_TIME) {
			System.out.println("[ CLIENT ] Fetching picture data from cache: " + key);
			return cache.get(album.getName() + "_" + picture.getName()).getData();
		}
		else {

			// Safe delete picture from cache
			if(cache.containsKey(key))
				cache.remove(key);

			// Retrieve cache again
			for(Request e : discovery.getServers().values()) {
				executed = false;
				for(int i = 0; !executed && i < MAX_RETRIES; i++) {
					try {
						//System.out.println("[ CLIENT ] Fetching picture data from server " + e.getAddress() + " : " + picture.getName());
						data = e.getPictureData(album, picture);
						if (data != null && data.length > 1) {
							SharedPicture cached_picture = new SharedPicture(picture.getName());
							cached_picture.setData(data);
							cache.put(album.getName() + '_' + picture.getName(), cached_picture);
							//System.out.println("[ CLIENT ] Fetched picture data from server: " + picture.getName() + "with size: " + data.length + " bytes");
							return data;
						}
						/*else
							System.out.println("[ CLIENT ] Fetched data from picture " + picture.getName() + " is null");*/
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
		if(current_data.containsKey(name) || name.isEmpty() || discovery.getServers().isEmpty())
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
					current_data.put(album.getName(), new ArrayList<>());
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
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					if(e.deleteAlbum(album) && current_data.containsKey(album.getName()))
						current_data.remove(album.getName());
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
		List<String> current_pictureList = current_data.get(album.getName());
		if(current_pictureList.contains(name) || discovery.getServers().isEmpty())
			return null;
		boolean executed;
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					String picture = e.uploadPicture(album, name, data);
					if (picture != null) {
						current_data.get(album.getName()).add(picture);
						return new SharedPicture(picture);
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
		if(discovery.getServers().isEmpty())
			return false;
		for(Request e : discovery.getServers().values()) {
			executed = false;
			for(int i = 0; !executed && i < MAX_RETRIES; i++) {
				try {
					if(e.deletePicture(album, picture)) {
						current_data.get(album.getName()).remove(picture.getName());
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

	private void setupConsumer() {
		props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "consumer-tutorial" + System.nanoTime());
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		consumer = new KafkaConsumer<>(props);
		current_topicList = new ArrayList<>();
		current_topicList.add("Albuns");
		consumer.subscribe(current_topicList);
	}

	private void detectChanges() {
		new Thread(() -> {
			try {
				for(;;) {
					ConsumerRecords<String, String> records = consumer.poll(1000);
					records.forEach( r -> {
						String topic = r.topic();
						String event = r.value();
						System.out.println("[ CLIENT ] Recieved topic: " + topic + " and event: " + event);
						if(topic.equalsIgnoreCase("Albuns") && !current_data.containsKey(event))
							updateAlbums();
						else if(current_data.containsKey(event))
							updateAlbum(new SharedAlbum(event));
					});
				}
			} finally {
				consumer.close();
			}
		}).start();
	}

	public List<String> updateAlbums() {
		System.out.println("[ CLIENT ] Updated Albuns!");
		List<String> lst_current = new ArrayList<>(current_data.keySet());
		List<String> lst_possible = getListOfAlbumsFromServers();
		if(!listsAreEqual(lst_current, lst_possible)) {
			current_data.clear();
			for(String album : lst_possible)
				current_data.put(album, new ArrayList<>());
			current_topicList.addAll(current_data.keySet());
			//consumer.subscribe(current_topicList);
			gui.updateAlbums();
		}
		return lst_current;
	}

	private List<String> updateAlbum(Album album) {
		System.out.println("[ CLIENT ] Updated album " + album.getName());
		List<String> lst_current = current_data.get(album.getName());
		List<String> lst_possible = getListOfPicturesFromServer(album);
		if(!listsAreEqual(lst_current, lst_possible)) {
			current_data.put(album.getName(), lst_possible);
			gui.updateAlbum(album);
		}
		return lst_current;
	}

	/**
	 * Sorts the lists and checks if the lists are equal
	 * @param lst1 - String list
	 * @param lst2 - String list
     * @return true if the lists are equal in size and elements or false otherwise
     */
	private Boolean listsAreEqual(List<String> lst1, List<String> lst2) {
		if(lst1 == null | lst2 == null)
			return false;
		Collections.sort(lst1);
		Collections.sort(lst2);
		return !(lst1.size() != lst2.size() || !lst1.equals(lst2));
	}

	/**
	 * Computes the time between two dates in time
	 * @param time1 - First date
	 * @param time2 - Second date
     * @return the number of minutes between time1 and time2
     */
	private double timeBetween(long time1, long time2) {
		long result = time2 - time1;
		return TimeUnit.MILLISECONDS.toMinutes(result);
	}

	/**
	 * Thread that runs along with the client and finds new ShareGalleryServers
	 * @throws IOException- In case the thread can't be created
     */
	private void findServers() throws IOException {
		if(discovery == null) {
			discovery = new SharedGalleryServerDiscovery(local_password, this);
			new Thread(discovery).start();
		}
	}

	/**
	 * Represents a shared album.
	 */
	private static class SharedAlbum implements GalleryContentProvider.Album {
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
	private static class SharedPicture implements GalleryContentProvider.Picture {
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