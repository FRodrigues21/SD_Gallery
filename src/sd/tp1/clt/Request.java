package sd.tp1.clt;

import sd.tp1.gui.GalleryContentProvider;

import java.util.List;

/**
 * Created by franciscorodrigues on 04/04/16.
 */
public interface Request {

    List<GalleryContentProvider.Album> getListOfAlbums();

    List<GalleryContentProvider.Picture> getListOfPictures(GalleryContentProvider.Album album);

    byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture);

    GalleryContentProvider.Album createAlbum(String name);

    void deleteAlbum(GalleryContentProvider.Album album);

    GalleryContentProvider.Picture uploadPicture(GalleryContentProvider.Album album, String name, byte [] data);

    Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture);

}
