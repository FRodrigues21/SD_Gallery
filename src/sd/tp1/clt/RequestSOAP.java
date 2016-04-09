package sd.tp1.clt;

import com.sun.xml.internal.ws.wsdl.parser.InaccessibleWSDLException;
import sd.tp1.clt.ws.*;
import sd.tp1.gui.GalleryContentProvider;

import java.net.*;
import java.util.List;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class RequestSOAP implements Request {

    private SharedGalleryServerSOAP server;
    private String url;
    private int tries;

    public RequestSOAP(String url) {
        this.tries = 0;
        this.url = url;
        try {
            server = new SharedGalleryServerSOAPService(new URL(url)).getSharedGalleryServerSOAPPort();
        } catch (MalformedURLException | InaccessibleWSDLException e) {
            System.out.println("BAD URL or INACCESSIBLE");
        }
    }

    public int getTries() {
        return tries++;
    }

    public String getAddress() {
        return url;
    }

    public List<String> getListOfAlbums() throws RuntimeException {
        return server.getListOfAlbums();
    }

    public List<String> getListOfPictures(GalleryContentProvider.Album album) throws RuntimeException{
        return server.getListOfPictures(album.getName());
    }

    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) throws RuntimeException {
        return server.getPictureData(album.getName(), picture.getName());
    }

    public String createAlbum(String name) throws RuntimeException {
        return server.createAlbum(name);
    }

    public Boolean deleteAlbum(GalleryContentProvider.Album album) throws RuntimeException {
        return server.deleteAlbum(album.getName());
    }

    public String uploadPicture(GalleryContentProvider.Album album, String name, byte [] data) throws RuntimeException {
        return server.uploadPicture(album.getName(), name, data);
    }

    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) throws RuntimeException {
        return server.deletePicture(album.getName(), picture.getName());
    }

}
