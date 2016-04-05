package sd.tp1.svr.SOAP;

import sd.tp1.gui.GalleryContentProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.stream.Collectors;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

@WebService
public class FileServerSOAP {

    private File basePath;

    // Sao iguais no rest
    private static String path = "./FileServer";
    private static MulticastSocket server_socket = null;
    private static InetAddress server_address = null;
    private static int server_port = 9000;

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
        if(dirPath.exists()) {
            List<FilePicture> pictures = Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f)).map(f -> new FilePicture(f)).collect(Collectors.toList());
            List<String> tmp = new ArrayList<>();
            for(FilePicture p : pictures){
                tmp.add(p.getName());
            }
            return tmp;
        }
        return null;
    }

    @WebMethod
    public byte [] getPictureData(String album, String picture){
        File dirPath = new File(basePath + "/" + album);
        if(dirPath.exists()) {
            List<FilePicture> pictures = Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f) && f.getName().equalsIgnoreCase(picture)).map(f -> new FilePicture(f)).collect(Collectors.toList());
            return pictures.get(0).getData();
        }
        return null;
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
        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8080/FileServer";
        Endpoint.publish(address_s, new FileServerSOAP(path));
        System.err.println("FileServerSOAP: Started @ " + address_s);

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
