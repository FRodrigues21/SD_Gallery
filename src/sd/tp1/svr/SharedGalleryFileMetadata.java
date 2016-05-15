package sd.tp1.svr;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by fxpro on 15/05/2016.
 */
public class SharedGalleryFileMetadata {

    private String basePath;
    private String album;
    private String picture;

    long updated;
    JSONArray operations;
    JSONArray servers;

    public SharedGalleryFileMetadata(String album, String picture, String path) {
        basePath = path + ".metadata";
        this.album = album;
        this.picture = picture;
        operations = new JSONArray();
        servers = new JSONArray();
        writeMetaData();
    }

    public void update(long time) {
        updated = time;
    }

    public boolean writeMetaData() {
        JSONObject root = new JSONObject();
        JSONObject data = new JSONObject();

        root.put("data", data);
        data.put("album", album);
        data.put("image", picture);
        data.put("updated", System.currentTimeMillis());
        data.put("operations", operations);
        data.put("servers", servers);

        FileWriter file;
        try {
            file = new FileWriter(new File(basePath));
            file.write(root.toJSONString());
            file.flush();
            file.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
