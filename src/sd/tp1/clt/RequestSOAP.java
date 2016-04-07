package sd.tp1.clt;

import sd.tp1.clt.ws.*;
import sd.tp1.gui.GalleryContentProvider;

import java.net.*;
import java.util.List;

/**
 * Created by franciscorodrigues on 03/04/16.
 */
public class RequestSOAP implements Request {

    private SharedGalleryServerSOAP server;

    public RequestSOAP(String url) {
        try {
            server = new SharedGalleryServerSOAPService(new URL(url)).getSharedGalleryServerSOAPPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // Gallery Methods

    public List<String> getListOfAlbums() {
        return server.getListOfAlbums();
    }

    public List<String> getListOfPictures(GalleryContentProvider.Album album) {
        return server.getListOfPictures(album.getName());
    }

    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return server.getPictureData(album.getName(), picture.getName());
    }

    public String createAlbum(String name) {
        return server.createAlbum(name);
    }

    public void deleteAlbum(GalleryContentProvider.Album album) {
        server.deleteAlbum(album.getName());
    }

    public String uploadPicture(GalleryContentProvider.Album album, String name, byte [] data) {
        return server.uploadPicture(album.getName(), name, data);
    }

    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return server.deletePicture(album.getName(), picture.getName());
    }

}
