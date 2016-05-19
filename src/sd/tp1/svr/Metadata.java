package sd.tp1.svr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fxpro on 15/05/2016.
 */
public class Metadata {

    private String type;
    private String name;
    private Operation last;
    private List<Operation> operations;
    private Map<String, Metadata> images;

    public Metadata(String type, String name) {
        this.type = type;
        this.name = name;
        operations = new LinkedList<>();
        images = new HashMap<>();
    }

    public Operation getLastUpdate() {
        return last;
    }

    public void addOperation(long cnt, long id, String event) {
        last = new Operation(cnt, id, event);
        operations.add(last);
        String result = String.format("[ METADATA ] %s: %s at (%d , %d)", name, event, cnt, id);
        System.out.println(result);
    }

    public boolean hasImage(String name) {
        return images.containsKey(name);
    }

    public void addImage(String name) {
        if(!hasImage(name))
            images.put(name, new Metadata("picture", name));
    }

    public void addImageOperation(String image, long cnt, long id, String event) {
        last = new Operation(cnt, id, event);
        images.get(image).addOperation(cnt, id, event);
    }

}
