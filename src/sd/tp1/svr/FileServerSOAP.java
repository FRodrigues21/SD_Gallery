package sd.tp1.svr;

import sd.tp1.gui.GalleryContentProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.stream.Collectors;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.crypto.Data;
import javax.xml.ws.Endpoint;

@WebService
public class FileServerSOAP {

    private File basePath;

    public FileServerSOAP() {
        this("./FileServer");
    }

    public FileServerSOAP(String path) {
        super();
        basePath = new File(path);
    }

    @WebMethod
    public List<String> getListOfAlbums() {
        List<String> tmp = new ArrayList<>();
        List<FileAlbum> albums = Arrays.asList(basePath.listFiles()).stream().filter(f -> f.isDirectory() && ! f.getName().endsWith(".deleted") && ! f.getName().startsWith(".")).map(f -> new FileAlbum(f)).collect(Collectors.toList());
        for (FileAlbum a : albums) {
            tmp.add(a.getName());
        }
        return tmp;
    }

    @WebMethod
    public List<String> getListOfPictures(String album){
        File dirPath = new File(basePath + "/" + album);
        List<FilePicture> pictures = Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f)).map(f -> new FilePicture(f)).collect(Collectors.toList());
        List<String> tmp = new ArrayList<>();
        for(FilePicture p: pictures){
          tmp.add(p.getName());
        }
        return tmp;
    }

    @WebMethod
    public byte [] getPictureData(String album, String picture){
        File dirPath = new File(basePath + "/" + album);
        List<FilePicture> pictures = Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f) && f.getName().equalsIgnoreCase(picture)).map(f -> new FilePicture(f)).collect(Collectors.toList());
        return pictures.get(0).getData();
    }

    @WebMethod
    public String createAlbum(String album){
        File dirPath = new File(basePath + "/" + album);
        if(!dirPath.exists()) {
            dirPath.mkdir();
            return album;
        }
        return null;
    }

    @WebMethod
    public void deleteAlbum(String album){
        File dirPath = new File(basePath + "/" + album);
        if(dirPath.exists())
            dirPath.renameTo(new File(dirPath.getAbsolutePath() + ".deleted"));
    }

    @WebMethod
    public String uploadPicture(String album, String picture, byte [] data){
        File filePath = new File(basePath + "/" + album + "/" + picture);
        if(!filePath.exists()) {
            try {
                Files.write(filePath.toPath(), data, StandardOpenOption.CREATE_NEW);
                return picture;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @WebMethod
    public Boolean deletePicture(String album, String picture){
        File filePath = new File(basePath + "/" + album + "/" + picture);
        if(filePath.exists()) {
            filePath.renameTo(new File(filePath.getAbsolutePath() + ".deleted"));
            return true;
        }
        return false;
    }


    public static void main(String args[]) throws Exception {
        String path = args.length > 0 ? args[0] : "./FileServer";
        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8080/FileServer";
        System.err.println(address_s);
        Endpoint.publish(address_s, new FileServerSOAP(path));
        System.err.println("FileServerSOAP: Started");

        //Create a multicast socket
        final int port = 9000;
        final InetAddress address = InetAddress.getByName("224.1.2.3");
        if (!address.isMulticastAddress()) {
            System.out.println("The address is not multicast!");
            System.exit(1);
        }
        MulticastSocket socket = new MulticastSocket(port);

        // Join a multicast group
        socket.joinGroup(address);

        // Wait for request and reply
        DatagramPacket incoming;
        DatagramPacket reply;
        while (true) {
            // Request received
            byte [] buffer = new byte[65536];
            incoming = new DatagramPacket(buffer, buffer.length);
            socket.receive(incoming);
            if(incoming.getLength() > 0) {
                String str_reply = new String(incoming.getData(), 0, incoming.getLength());
                System.out.println("Request received: " + str_reply);
                if (str_reply.contains("FileServer")) {
                    // Reply with address
                    reply = new DatagramPacket(address_s.getBytes(), address_s.getBytes().length);
                    reply.setPort(incoming.getPort());
                    reply.setAddress(incoming.getAddress());
                    System.out.println("Sending reply: " + new String(reply.getData(), 0, reply.getLength()));
                    socket.send(reply);
                }
            }
        }
    }

    static class FileAlbum implements GalleryContentProvider.Album {
        final File dir;

        FileAlbum(File dir) {
            this.dir = dir;
        }

        @Override
        public String getName() {
            return dir.getName();
        }
    }

    static class FilePicture implements GalleryContentProvider.Picture {
        final File file;

        FilePicture(File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        byte[] getData() {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    static boolean isPicture(File f) {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        String ext = i < 0 ? "" : filename.substring(i + 1).toLowerCase();
        return f.isFile() && EXTENSIONS.contains(ext) && !filename.startsWith(".") && !filename.endsWith(".deleted");
    }

    static final List<String> EXTENSIONS = Arrays.asList(new String[] { "jpg", "jpeg", "png" });

}
