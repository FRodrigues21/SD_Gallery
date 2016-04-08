package sd.tp1.clt;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryContentCache<K,V> extends LinkedHashMap<K,V> {

    private int capacity;

    public SharedGalleryContentCache(int capacity) {
        super(capacity + 1, 0.75F, true);
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > capacity;
    }

}
