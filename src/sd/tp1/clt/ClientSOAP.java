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

    public ClientSOAP() throws IOException {
        multicast_socket = new MulticastSocket();
        setAddress();
        server = new FileServerSOAPService(findFileServer()).getFileServerSOAPPort();
        System.err.println("ClientSOAPS: Started");
    }

    // Gallery Methods

    public List<GalleryContentProvider.Album> getListOfAlbums() {
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

    public void deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        server.deletePicture(album.getName(), picture.getName());
    }

    // Connection Methods

    public URL findFileServer() {
        try {
            return new URL(sendAndWait("FileServer"));
        } catch (MalformedURLException e) {
            System.exit(1);
        }
        return null;
    }

    private String sendAndWait(String message) {
        byte [] data = message.getBytes();
        DatagramPacket request = new DatagramPacket(data, data.length, client_address, client_port);
        try {
            multicast_socket.send(request);
            return getSocketReply();
        } catch (IOException e) {
            return null;
        }
    }

    private String getSocketReply() {
        DatagramPacket reply;
        do {
            byte [] buffer = new byte[65536];
            reply = new DatagramPacket(buffer, buffer.length);
            try {
                multicast_socket.receive(reply);
                if(reply.getLength() > 0) {
                    buffer = reply.getData();
                    return new String(buffer, 0, buffer.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (reply.getLength() == 0);
        return null;
    }

    private boolean setAddress() {
        client_port = 9000;
        try {
            client_address = InetAddress.getByName("224.1.2.3");
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

}
