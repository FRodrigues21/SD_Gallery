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

    private int cnt;
    private String name;
    private Operation last;

    public Metadata(String name) {
        this.name = name;
    }

    public void addOperation(long id, String event) {
        last = new Operation(cnt++, id, event);
    }

    public String toString() {
        return String.format("%s %d %d %s", name, last.getCnt(), last.getId(), last.getEvent());
    }

}
