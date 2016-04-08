package sd.tp1.clt;

import sd.tp1.gui.GalleryContentProvider;

import java.util.List;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public interface Request {

    int getTries();

    String getAddress();

    List<String> getListOfAlbums();

    List<String> getListOfPictures(GalleryContentProvider.Album album);

    byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture);

    String createAlbum(String name);

    void deleteAlbum(GalleryContentProvider.Album album);

    String uploadPicture(GalleryContentProvider.Album album, String name, byte [] data);

    Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture);

}
