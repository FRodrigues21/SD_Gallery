package sd.tp1.svr;

/**
 * Created by fxpro on 15/05/2016.
 */
public class Metadata {

    private int cnt;
    private long id;
    private String path;
    private String event;

    public Metadata(String path) {
        this.path = path;
    }

    public void addOperation(long id, String event) {
        this.cnt++;
        this.id = id;
        this.event = event;
    }

    public String converted() {
        return String.format("%s %d %d %s", path, cnt, id, event);
    }

}
