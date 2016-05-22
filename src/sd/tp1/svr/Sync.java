package sd.tp1.svr;

import sd.tp1.clt.Request;

import java.util.List;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public interface Sync extends Request {

    List<String> sync();

}
