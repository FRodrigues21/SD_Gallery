package sd.tp1.svr.SOAP;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import sd.tp1.svr.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.core.Response;
import javax.xml.ws.Endpoint;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
@WebService
public class SharedGalleryServerSOAP {

    private static File basePath;
    private static String local_password;

    private static long id;

    private static KafkaProducer<String, String> producer;
    private static OthersServerDiscovery discovery;
    private static MetadataController metadata_controller;

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
        if(validate(password)) {
            if(album.equalsIgnoreCase(SharedGalleryFileSystemUtilities.createDirectory(basePath, album))) {
                // Metadata
                String path = "/" + album;
                metadata_controller.add(path, id, "create", "null");
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                //Kafka
                sendToConsumers("Albuns", album + "-create");
                return album;
            }
        }
        return null;
    }

    @WebMethod
    public Boolean deleteAlbum(String album, String password){
        if(validate(password)) {
            boolean deleted = SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
            if(deleted) {
                // Metadata
                String path = "/" + album;
                metadata_controller.addOp(path, id, "delete");
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                //Kafka
                sendToConsumers("Albuns", album + "-delete");
                return true;
            }
        }
        return false;
    }

    @WebMethod
    public String uploadPicture(String album, String picture, byte [] data, String password) {
        if(validate(password)) {
            String new_name = SharedGalleryFileSystemUtilities.createPicture(basePath, album, picture, data);
            if(new_name != null && picture.equalsIgnoreCase(new_name)) {
                String no_ext = SharedGalleryFileSystemUtilities.removeExtension(picture);

                // Metadata
                String path = "/" + album + "/" + no_ext;
                metadata_controller.add(path, id, "create", picture);
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                // Kafka
                sendToConsumers(album, no_ext + "-" + "create");
                return SharedGalleryFileSystemUtilities.removeExtension(new_name);
            }
        }
        return null;
    }

    @WebMethod
    public Boolean deletePicture(String album, String picture, String password) {
        if(validate(password)) {
            boolean deleted = SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
            if(deleted) {
                // Metadata
                String path = "/" + album + "/" + picture;
                metadata_controller.addOp(path, id, "delete");
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                // Kafka
                sendToConsumers(album, picture + "-" + "delete");
                return true;
            }
        }
        return false;
    }

    @WebMethod
    public List<String> sendMetadata(String password) {
        if(validate(password)) {
            List<String> current_metadata = new ArrayList<>();
            for(Metadata metadata : ((ConcurrentHashMap<String, Metadata>)metadata_controller.getMetadata()).values()) {
                String content = metadata.converted();
                current_metadata.add(content);
                System.out.println("[ SENDING ] " + content);
            }
            return current_metadata;
        }
        return null;
    }

    @SuppressWarnings("ALL")
    private static void fetchReplicaMetadata() {
        new Thread(() -> {
            for(;;) {
                List<String> content = new ArrayList<>();
                Sync request = discovery.getServer();
                if(request != null) {
                    content = request.sync();
                    compareMetadata(request, content);
                }
                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressWarnings("ALL")
    private static void compareMetadata(Sync request, List<String> replica_content) {
        for(String metadata : replica_content) {
            System.out.println("[ RECIEVING ] " + metadata);
            String [] data = metadata.split(" ");
            String path = data[0];
            Metadata tmp = new Metadata(path, Integer.parseInt(data[1]), Long.parseLong(data[2]), data[3], data[4]);
            String album = albumFromMetadata(path);
            String event = tmp.getEvent();
            if(metadata_controller.getMetadata().containsKey(path)) { // Tem metadata
                Metadata local = (Metadata)metadata_controller.getMetadata().get(path);
                if(local.getCnt() < tmp.getCnt()) {
                    if(isAlbumMetadata(path)) {
                        metadata_controller.addFrom(path, tmp);
                        if(tmp.getEvent().equalsIgnoreCase("delete")) {
                            SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
                        }
                        else
                            SharedGalleryFileSystemUtilities.createDirectory(basePath, album);
                    }
                    else {
                        if(!metadata_controller.getMetadata().containsKey("/" + album)) {
                            metadata_controller.add("/" + album, id, "create", "null");
                            SharedGalleryFileSystemUtilities.createDirectory(basePath, albumFromMetadata(path));
                        }
                        String picture = pictureFromMetadata(path);
                        String ext = data[4];
                        metadata_controller.addFrom(path, tmp);
                        if(tmp.getEvent().equalsIgnoreCase("delete")) {
                            SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
                            sendToConsumers(album, picture + "-delete");
                        }
                        else {
                            SharedGalleryFileSystemUtilities.createPicture(basePath, album, ext, request.getPictureData(album, picture));
                        }
                    }
                }
                else if(local.getCnt() == tmp.getCnt()) {
                    if(!local.getEvent().equalsIgnoreCase(tmp.getEvent())) {
                        if(isAlbumMetadata(path)) {
                            if(tmp.getEvent().equalsIgnoreCase("delete")) {
                                metadata_controller.addFrom(path, tmp);
                                SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
                                sendToConsumers("Albuns", album + "-delete");
                            }
                        }
                        else {
                            String picture = albumFromMetadata(path);
                            if(tmp.getEvent().equalsIgnoreCase("delete")) {
                                metadata_controller.addFrom(path, tmp);
                                SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
                                sendToConsumers(album, picture + "-delete");
                            }
                        }
                    }
                }
            }
            else { // Não tem metadata
                if(isAlbumMetadata(path)) {
                    metadata_controller.addFrom(path, tmp);
                    if(event.equalsIgnoreCase("create"))
                        SharedGalleryFileSystemUtilities.createDirectory(basePath, albumFromMetadata(path));
                }
                else {
                    if(!metadata_controller.getMetadata().containsKey("/" + album)) {
                        metadata_controller.add("/" + album, id, "create", "null");
                        SharedGalleryFileSystemUtilities.createDirectory(basePath, albumFromMetadata(path));
                    }
                    String picture = pictureFromMetadata(path);
                    String ext = data[4];
                    metadata_controller.addFrom(path, tmp);
                    if(event.equalsIgnoreCase("create")) {
                        System.out.println("[ CREATING NON PICTURE ] " + ext);
                        SharedGalleryFileSystemUtilities.createPicture(basePath, album, ext, request.getPictureData(album, picture));
                    }
                }
            }
        }
    }

    private static boolean isAlbumMetadata(String name) {
        int count = name.length() - name.replaceAll("/", "").length();
        if(count == 2)
            return false;
        return true;
    }

    private static String pictureFromMetadata(String meta) {
        String [] result = meta.split("/");
        return result[2];
    }

    private static String albumFromMetadata(String meta) {
        String [] result = meta.split("/");
        return result[1];
    }

    private static boolean validate(String password) {
        return password.equalsIgnoreCase(local_password);
    }

    private static void sendToConsumers(String topic, String event) {
        producer.send(new ProducerRecord<>(topic, event));
        System.out.println("[ PROXY ] Sending event to consumer: " + topic + " " + event);
    }

    public static void main(String args[]) throws Exception {

        int port;

        id = System.nanoTime();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("PATH: ");
        basePath = new File("./FileServerSOAP" + reader.readLine());

        System.out.println("KAFKA IP: ");
        String kafka_ip = reader.readLine();

        System.out.println("PORT: ");
        port = Integer.parseInt(reader.readLine());

        System.out.println("LOCAL PASSWORD: ");
        local_password = reader.readLine();

        basePath.mkdir();

        String address_s = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/FileServerSOAP";
        Endpoint.publish(address_s, new SharedGalleryServerSOAP());

        // Event dessimination - Producer

        Properties env = System.getProperties();
        Properties props = new Properties();

        props.put("zk.connect", env.getOrDefault("zk.connect", kafka_ip+":2181/"));
        props.put("bootstrap.servers", env.getOrDefault("bootstrap.servers", kafka_ip+":9092"));
        props.put("log.retention.ms", 1000);

        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);

        System.err.println("SharedGalleryServerSOAP: Started @ " + address_s);

        discovery = new OthersServerDiscovery(address_s, local_password);
        new Thread(discovery).start();

        new Thread(new SharedGalleryClientDiscovery(address_s)).start();

        metadata_controller = new MetadataController(basePath);
        metadata_controller.load();

        fetchReplicaMetadata();

    }

}
