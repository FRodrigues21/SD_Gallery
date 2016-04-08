package sd.tp1.clt;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by franciscorodrigues on 08/04/16.
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
