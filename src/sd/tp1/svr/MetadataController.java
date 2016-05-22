package sd.tp1.svr;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by franciscorodrigues on 22/05/16.
 */

public class MetadataController implements java.io.Serializable {

    private static Map<String, Metadata> metadata;
    private File basePath;

    public MetadataController(File basePath) {
        this.basePath = basePath;
        metadata = new ConcurrentHashMap<>();
    }

    public Map getMetadata() {
        return metadata;
    }

    public void add(String path, long id, String event, String ext) {
        metadata.put(path, new Metadata(path, ext));
        metadata.get(path).addOperation(id, event);
        save();
    }

    public void addFrom(String path, Metadata data) {
        metadata.put(path, data);
        save();
    }

    public void addOp(String path, long id, String event) {
        if(metadata.containsKey(path))
            metadata.get(path).addOperation(id, event);
        save();
    }

    public String metadata(String path) {
        return metadata.get(path).converted();
    }

    private void save(){
        try {
            FileOutputStream f = new FileOutputStream(basePath.getAbsolutePath()+ File.separator + "files.metadata");
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(metadata);
            s.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        Map<String, Metadata> tmp = new ConcurrentHashMap<>();
        try{
            FileInputStream f = new FileInputStream(basePath.getAbsolutePath()+ File.separator + "files.metadata");
            ObjectInputStream s = new ObjectInputStream(f);
            tmp = (ConcurrentHashMap<String, Metadata>) s.readObject();
            s.close();
        } catch (Exception e) {
            save();
        }
        metadata = tmp;
    }

}
