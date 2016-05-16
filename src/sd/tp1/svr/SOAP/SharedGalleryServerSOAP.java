package sd.tp1.svr.SOAP;

import sd.tp1.svr.SharedGalleryFileSystemUtilities;
import sd.tp1.svr.SharedGalleryClientDiscovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
    private static String local_password;

    /**
     * The methods from this class act the same way as the ones from REQUEST interface
     */

    @WebMethod
    public List<String> getListOfAlbums() {
        return SharedGalleryFileSystemUtilities.getDirectoriesFromPath(basePath);
    }

    @WebMethod
    public List<String> getListOfPictures(String album, String password) {
        if(validate(password))
            return SharedGalleryFileSystemUtilities.getPicturesFromDirectory(basePath, album);
        return null;
    }

    @WebMethod
    public byte [] getPictureData(String album, String picture, String password) {
        if(validate(password))
            return SharedGalleryFileSystemUtilities.getDataFromPicture(basePath, album, picture);
        return null;
    }

    @WebMethod
    public String createAlbum(String album, String password) {
        if(validate(password))
            return SharedGalleryFileSystemUtilities.createDirectory(basePath, album);
        return null;
    }

    @WebMethod
    public Boolean deleteAlbum(String album, String password){
        if(validate(password))
            return SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
        return false;
    }

    @WebMethod
    public String uploadPicture(String album, String picture, byte [] data, String password) {
        if(validate(password))
            return SharedGalleryFileSystemUtilities.createPicture(basePath, album, picture, data);
        return null;
    }

    @WebMethod
    public Boolean deletePicture(String album, String picture, String password) {
        if(validate(password))
            return SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
        return false;
    }

    private static boolean validate(String password) {
        return password.equalsIgnoreCase(local_password);
    }

    public static void main(String args[]) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("LOCAL PASSWORD: ");
        local_password = reader.readLine();

        basePath.mkdir();

        String address_s = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/FileServerSOAP";
        Endpoint.publish(address_s, new SharedGalleryServerSOAP());

        System.err.println("SharedGalleryServerSOAP: Started @ " + address_s);

        // Receives
        new Thread(new SharedGalleryClientDiscovery(address_s)).start();

    }

}
