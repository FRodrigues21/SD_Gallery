package sd.tp1.svr.SOAP;

import sd.tp1.svr.SharedGalleryFileSystemUtilities;
import sd.tp1.svr.SharedGalleryClientDiscovery;

import java.io.File;
import java.net.*;
import java.util.*;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
@WebService
public class SharedGalleryServerSOAP {

    private static File basePath = new File("./FileServerSOAP");

    public SharedGalleryServerSOAP() { }

    /**
     * The methods from this class act the same way as the ones from REQUEST interface
     */

    @WebMethod
    public List<String> getListOfAlbums() {
        return SharedGalleryFileSystemUtilities.getDirectoriesFromPath(basePath);
    }

    @WebMethod
    public List<String> getListOfPictures(String album) {
        return SharedGalleryFileSystemUtilities.getPicturesFromDirectory(basePath, album);
    }

    @WebMethod
    public byte [] getPictureData(String album, String picture) {
        return SharedGalleryFileSystemUtilities.getDataFromPicture(basePath, album, picture);
    }

    @WebMethod
    public String createAlbum(String album) {
        return SharedGalleryFileSystemUtilities.createDirectory(basePath, album);
    }

    @WebMethod
    public Boolean deleteAlbum(String album){
        return SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
    }

    @WebMethod
    public String uploadPicture(String album, String picture, byte [] data) {
        return SharedGalleryFileSystemUtilities.createPicture(basePath, album, picture, data);
    }

    @WebMethod
    public Boolean deletePicture(String album, String picture) {
        return SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
    }

    public static void main(String args[]) throws Exception {

        if(!basePath.exists())
            basePath.mkdir();

        // Get local address and publish
        String address_s = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/FileServerSOAP";
        Endpoint.publish(address_s, new SharedGalleryServerSOAP());

        System.err.println("SharedGalleryServerSOAP: Started @ " + address_s);

        // Receives
        Thread r = new Thread(new SharedGalleryClientDiscovery(address_s));
        r.start();

    }

}
