package sd.tp1.svr.SOAP;

import sd.tp1.svr.FileSystemUtilities;

import java.io.File;
import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

@WebService
public class SharedGalleryServerSOAP {

    private File basePath;

    // Sao iguais no rest
    private static String path = "./FileServerSOAP";
    private static MulticastSocket server_socket = null;
    private static InetAddress server_address = null;
    private static int server_port = 9000;

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

    private static void sendIPToClient(String reply, InetAddress client_address, int client_port) {
        DatagramPacket p = new DatagramPacket(reply.getBytes(), reply.getBytes().length, client_address, client_port);
        try {
            server_socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {

        // Get local address and publish
        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8080/FileServerSOAP";
        Endpoint.publish(address_s, new SharedGalleryServerSOAP(path));
        System.err.println("SharedGalleryServerSOAP: Started @ " + address_s);

        //Create a multicast socket
        server_address = InetAddress.getByName("224.1.2.3");
        if (!server_address.isMulticastAddress()) {
            System.out.println("The address is not multicast!");
            System.exit(1);
        }
        server_socket = new MulticastSocket(server_port);

        // Join a multicast group
        server_socket.joinGroup(server_address);

        // Receives
        Thread r = new Thread(new Runnable(){
            @Override
            public void run() {
                System.err.println("RECEIVING THREAD STARTED!");
                while(true) {
                    try {
                        byte [] buffer = new byte[65536];
                        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                        server_socket.receive(incoming);
                        if(incoming.getLength() > 0) {
                            String incoming_message = new String(incoming.getData(), 0, incoming.getLength());
                            System.out.println("Request received: " + incoming_message);
                            if (incoming_message.contains("FileServer"))
                                sendIPToClient(address_s, incoming.getAddress(), incoming.getPort());
                        }
                        else
                            Thread.sleep(5000);
                    }
                    catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        r.start();

    }

}
