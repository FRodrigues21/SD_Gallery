package sd.tp1.svr.REST;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by franciscorodrigues on 12/05/16.
 */
public class SharedGalleryImgurAlbum {

    private String id;
    private String title;

    private Map<String, String> pictures; // Nome,ID

    public SharedGalleryImgurAlbum(String id, String title) {
        this.id = id;
        this.title = title;
        pictures = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public boolean hasPicture(String name) {
        return pictures.containsKey(name);
    }

    public String getPictureId(String name) {
        return pictures.get(name);
    }

    public void clear() {
        pictures.clear();
    }

    public void addPicture(String name, String id) {
        pictures.put(name, id);
    }

    public Map<String, String> getPictures() { return pictures; }

}
