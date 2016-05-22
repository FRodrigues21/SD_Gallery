package sd.tp1.svr;

import sd.tp1.clt.Request;
import sd.tp1.gui.GalleryContentProvider;

import java.util.List;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public interface Sync {

    List<String> sync();

    int getTries();

    String getAddress();

    List<String> getListOfAlbums();

    List<String> getListOfPictures(String album);

    byte[] getPictureData(String album, String picture);

    String createAlbum(String name);

    Boolean deleteAlbum(String album);

    String uploadPicture(String album, String name, byte [] data);

    Boolean deletePicture(String album, String picture);

}
