package sd.tp1.svr;

/**
 * Created by fxpro on 15/05/2016.
 */
public class Metadata implements java.io.Serializable {

    private int cnt;
    private long id;
    private String path;
    private String event;
    private String ext;

    public Metadata(String path, int cnt, long id, String event, String ext) {
        this.path = path;
        this.cnt = cnt;
        this.id = id;
        this.event = event;
        this.ext = ext;
    }

    public Metadata(String path, String ext) {
        this.ext = ext;
        this.path = path;
    }

    public void addOperation(long id, String event) {
        this.cnt++;
        this.id = id;
        this.event = event;
    }

    public void addFromOperation(long id, int cnt, String event) {
        this.cnt = cnt;
        this.id = id;
        this.event = event;
    }

    public int getCnt() {
        return cnt;
    }

    public String getEvent() {
        return event;
    }

    public String getExt() {
        return ext;
    }

    public String converted() {
        return String.format("%s %d %d %s %s", path, cnt, id, event, ext);
    }

}
