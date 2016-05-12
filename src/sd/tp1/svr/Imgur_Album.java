package sd.tp1.svr;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by franciscorodrigues on 12/05/16.
 */
public class Imgur_Album {

    private String id;
    private String title;

    private Map<String, String> pictures; // Nome,ID

    public Imgur_Album(String id, String title) {
        this.id = id;
        this.title = title;
        pictures = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getPictureId(String name) {
        return pictures.get(name);
    }

    public void addPicture(String name, String id) {
        pictures.put(name, id);
    }

}
