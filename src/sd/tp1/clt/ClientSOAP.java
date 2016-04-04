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

    private URL server_url = null;
    private InetAddress client_address;
    private int client_port;
    private MulticastSocket multicast_socket;
    private FileServerSOAP server;

    public ClientSOAP(String url) {
        System.err.println("ClientSOAPS: Created from " + url);
        try {
            server = new FileServerSOAPService(new URL(url)).getFileServerSOAPPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // Gallery Methods

    public List<GalleryContentProvider.Album> getListOfAlbums() {
        System.err.println("EXECUTOU DENTRO");
        List<GalleryContentProvider.Album> lst = new ArrayList<>();
        for (String s : server.getListOfAlbums()) {
            lst.add(new SharedAlbum(s));
        }
        return lst;
    }

    public List<GalleryContentProvider.Picture> getListOfPictures(GalleryContentProvider.Album album) {
        List<GalleryContentProvider.Picture> lst = new ArrayList<>();
        for (String s : server.getListOfPictures(album.getName())) {
            lst.add(new SharedPicture(s));
        }
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
