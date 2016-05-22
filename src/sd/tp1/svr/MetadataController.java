package sd.tp1.svr;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by franciscorodrigues on 22/05/16.
 */

public class MetadataController {

    private static ConcurrentHashMap<String, Metadata> metadata;

    public MetadataController() {
        metadata = new ConcurrentHashMap<>();
    }

    public void add(String path) {
        metadata.put(path, new Metadata(path));
    }

    public void addOp(String path, long id, String event) {
        if(metadata.containsKey(path))
            metadata.get(path).addOperation(id, event);
    }

    public String metadata(String path) {
        return metadata.get(path).toString();
    }

}
