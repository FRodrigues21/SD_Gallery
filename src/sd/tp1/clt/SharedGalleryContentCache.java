package sd.tp1.clt;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryContentCache<K,V> extends LinkedHashMap<K,V> {

    private int capacity; // Maximum number of items contained in the cache

    public SharedGalleryContentCache(int capacity) {
        super(capacity + 1, 0.75F, true);
        this.capacity = capacity;
    }

    /**
     * Removes the eldest entry from the cache when the cache reaches full capacity and the client is trying to add one more item
     * @param eldest - Eldest entry of the cache
     * @return true if the entry is the eldest or false otherwise
     */
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > capacity;
    }

}
