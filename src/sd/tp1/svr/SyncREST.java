package sd.tp1.svr;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public class SyncREST implements Sync {

    private String url;
    private String password;

    public SyncREST(String url, String password) {
        this.url = url;
        this.password = password;
    }

    @Override
    public Boolean compareMetadata(String metadata) {
        return null;
    }

}
