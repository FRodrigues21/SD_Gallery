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

    private SharedGalleryServerSOAP server; // Stub from the SOAP server
    private String url; // Url of the SOAP server
    private int tries; // Number of failed tries to make a request/method
    private String local_password;

    public RequestSOAP(String url, String password) {
        this.tries = 0;
        this.url = url;
        this.local_password = password;
        try {
            server = new SharedGalleryServerSOAPService(new URL(url)).getSharedGalleryServerSOAPPort();
        } catch (MalformedURLException | InaccessibleWSDLException e) {
            System.out.println("CLIENT ERROR: BAD URL or INACCESSIBLE");
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
        return server.getListOfPictures(album.getName(), local_password);
    }

    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) throws RuntimeException {
        return server.getPictureData(album.getName(), picture.getName(), local_password);
    }

    public String createAlbum(String name) throws RuntimeException {
        return server.createAlbum(name, local_password);
    }

    public Boolean deleteAlbum(GalleryContentProvider.Album album) throws RuntimeException {
        return server.deleteAlbum(album.getName(), local_password);
    }

    public String uploadPicture(GalleryContentProvider.Album album, String name, byte [] data) throws RuntimeException {
        return server.uploadPicture(album.getName(), name, data, local_password);
    }

    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) throws RuntimeException {
        return server.deletePicture(album.getName(), picture.getName(), local_password);
    }

}
