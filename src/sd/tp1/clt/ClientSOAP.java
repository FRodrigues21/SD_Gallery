package sd.tp1.clt;

import sd.tp1.clt.ws.*;
import sd.tp1.gui.GalleryContentProvider;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franciscorodrigues on 03/04/16.
 */
public class ClientSOAP implements Client {

    private FileServerSOAP server;

    public ClientSOAP(String url) {
        try {
            server = new FileServerSOAPService(new URL(url)).getFileServerSOAPPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // Gallery Methods

    public List<GalleryContentProvider.Album> getListOfAlbums() {
        List<GalleryContentProvider.Album> lst = new ArrayList<>();
        List<String> tmp = server.getListOfAlbums();
        if(tmp == null)
            return null;
        for (String s : tmp) {
            lst.add(new SharedAlbum(s));
        }
        return lst;
    }

    public List<GalleryContentProvider.Picture> getListOfPictures(GalleryContentProvider.Album album) {
        List<GalleryContentProvider.Picture> lst = new ArrayList<>();
        List<String> tmp = server.getListOfPictures(album.getName());
        if(tmp == null)
            return null;
        for (String s : tmp)
                lst.add(new SharedPicture(s));
        return lst;
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
