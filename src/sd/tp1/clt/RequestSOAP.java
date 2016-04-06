package sd.tp1.clt;

import sd.tp1.clt.ws.*;
import sd.tp1.gui.GalleryContentProvider;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franciscorodrigues on 03/04/16.
 */
public class RequestSOAP implements Request {

    private FileServerSOAP server;

    public RequestSOAP(String url) {
        try {
            server = new FileServerSOAPService(new URL(url)).getFileServerSOAPPort();
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

    public GalleryContentProvider.Album createAlbum(String name) {
        return new SharedAlbum(server.createAlbum(name));
    }

    public void deleteAlbum(GalleryContentProvider.Album album) {
        server.deleteAlbum(album.getName());
    }

    public GalleryContentProvider.Picture uploadPicture(GalleryContentProvider.Album album, String name, byte [] data) {
        return new SharedPicture(server.uploadPicture(album.getName(), name, data));
    }

    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return server.deletePicture(album.getName(), picture.getName());
    }

}
