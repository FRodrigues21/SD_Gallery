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
        File dirPath = new File(basePath + "/" + album);
        return FileSystemUtilities.getPicturesFromDirectory(dirPath);
    }

    @WebMethod
    public byte [] getPictureData(String album, String picture){
        File imgPath = new File(basePath + "/" + album + "/" + picture);
        return FileSystemUtilities.getDataFromPicture(imgPath);
    }

    @WebMethod
    public String createAlbum(String album){
        File dirPath = new File(basePath + "/" + album);
        return FileSystemUtilities.createDirectory(dirPath);
    }

    @WebMethod
    public void deleteAlbum(String album){
        File dirPath = new File(basePath + "/" + album);
        FileSystemUtilities.deleteDirectory(dirPath);
    }

    @WebMethod
    public String uploadPicture(String album, String picture, byte [] data){
        File filePath = new File(basePath + "/" + album + "/" + picture);
        return FileSystemUtilities.createPicture(filePath, data);
    }

    @WebMethod
    public Boolean deletePicture(String album, String picture){
        File filePath = new File(basePath + "/" + album + "/" + picture);
        return FileSystemUtilities.deletePicture(filePath);
    }

    public static void main(String args[]) throws Exception {

        // Get local address and publish
        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8080/FileServerSOAP";
        Endpoint.publish(address_s, new SharedGalleryServerSOAP(path));
        System.err.println("SharedGalleryServerSOAP: Started @ " + address_s);

        // Receives
        Thread r = new Thread(new SharedGalleryClientDiscovery(address_s));
        r.start();

    }

}
