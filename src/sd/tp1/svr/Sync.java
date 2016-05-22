package sd.tp1.svr;

import sd.tp1.clt.Request;
import sd.tp1.gui.GalleryContentProvider;

import java.util.List;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public interface Sync {

    List<String> sync();

    byte[] getPictureData(String album, String picture);

}
