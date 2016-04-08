package sd.tp1.svr.SOAP;

import sd.tp1.svr.FileSystemUtilities;
import sd.tp1.svr.SharedGalleryClientDiscovery;

import java.io.File;
import java.util.*;
import java.net.InetAddress;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

@WebService
public class SharedGalleryServerSOAP {

    private File basePath;

    private static String path = "./FileServerSOAP";

    public SharedGalleryServerSOAP() {
        this("./FileServerSOAP");
    }

    public SharedGalleryServerSOAP(String path) {
        super();
        basePath = new File(path);
    }

    @WebMethod
    public List<String> getListOfAlbums() {
        return FileSystemUtilities.getDirectoriesFromPath(basePath);
    }

    @WebMethod
    public List<String> getListOfPictures(String album){
        return FileSystemUtilities.getPicturesFromDirectory(basePath, album);
    }

    @WebMethod
    public byte [] getPictureData(String album, String picture){
        return FileSystemUtilities.getDataFromPicture(basePath, album, picture);
    }

    @WebMethod
    public String createAlbum(String album){
        return FileSystemUtilities.createDirectory(basePath, album);
    }

    @WebMethod
    public void deleteAlbum(String album){
        FileSystemUtilities.deleteDirectory(basePath, album);
    }

    @WebMethod
    public String uploadPicture(String album, String picture, byte [] data){
        return FileSystemUtilities.createPicture(basePath, album, picture, data);
    }

    @WebMethod
    public Boolean deletePicture(String album, String picture){
        return FileSystemUtilities.deletePicture(basePath, album, picture);
    }

    public static void main(String args[]) throws Exception {

        // Get local address and publish
        String address_s = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/FileServerSOAP/";
        Endpoint.publish(address_s, new SharedGalleryServerSOAP(path));
        System.err.println("SharedGalleryServerSOAP: Started @ " + address_s);

        // Receives
        Thread r = new Thread(new SharedGalleryClientDiscovery(address_s));
        r.start();

    }

}
