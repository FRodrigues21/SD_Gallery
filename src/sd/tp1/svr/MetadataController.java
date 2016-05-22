package sd.tp1.svr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by franciscorodrigues on 22/05/16.
 */

public class MetadataController {

    private static Map<String, Metadata> metadata;

    public MetadataController() {
        metadata = new ConcurrentHashMap<>();
    }


    public Map getMetadata() {
        return metadata;
    }

    public void add(String path, long id, String event, String ext) {
        metadata.put(path, new Metadata(path, ext));
        metadata.get(path).addOperation(id, event);
    }

    public void addFrom(String path, Metadata data) {
        metadata.put(path, data);
    }

    public void addOp(String path, long id, String event) {
        if(metadata.containsKey(path))
            metadata.get(path).addOperation(id, event);
    }

    public String metadata(String path) {
        return metadata.get(path).converted();
    }

}
