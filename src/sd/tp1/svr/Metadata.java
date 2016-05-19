package sd.tp1.svr;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fxpro on 15/05/2016.
 */
public class Metadata {

    private String data;
    private Clock updated;

    List<Clock> operations;

    public Metadata(String data) {
        this.data = data;
        operations = new LinkedList<>();
    }

    public Clock getLastUpdate() {
        return updated;
    }

    public void lastUpdate(long cnt, long id, String event) {
        updated = new Clock(cnt, id, event);
    }

    public void addOperation(long cnt, long id, String event) {
        lastUpdate(cnt, id, event);
        operations.add(updated);
        System.out.println("[ " + data + " ] " + "(" + cnt + " , " + id + ")");
    }

}
