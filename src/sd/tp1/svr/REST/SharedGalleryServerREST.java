package sd.tp1.svr.REST;

import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd.tp1.svr.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
@Path("/")
public class SharedGalleryServerREST {

    private static File basePath = new File("./FileServerREST"); // Path where the server files are
    private static String local_password;
    private static final File KEYSTORE = new File("./server.jks");

    private static long id;

    private static KafkaProducer<String, String> producer;
    private static MetadataController metadata_controller;
    private static OthersServerDiscovery discovery;

    /**
     * The methods from this class act the same way as the ones from REQUEST interface, but instead of null return an error status code
     */

    @GET
    @Path("password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums(@PathParam("password") String password) {
        if(validate(password)) {
            List<String> lst = SharedGalleryFileSystemUtilities.getDirectoriesFromPath(basePath);
            if(lst != null) {
                return Response.ok(lst).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("&password={password}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAlbum(@PathParam("password") String password, String album) {
        if(validate(password)) {
            if(album.equalsIgnoreCase(SharedGalleryFileSystemUtilities.createDirectory(basePath, album))) {

                // Metadata
                String path = "/" + album;
                metadata_controller.add(path, id, "create", "null");
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                //Kafka
                sendToConsumers("Albuns", album + "-create");

                return Response.status(Response.Status.CREATED).entity(album).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @DELETE
    @Path("/{album}&password={password}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("album") String album, @PathParam("password") String password) {
        if(validate(password)) {
            boolean created = SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
            if(created) {

                // Metadata
                String path = "/" + album;
                metadata_controller.addOp(path, id, "delete");
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                //Kafka
                sendToConsumers("Albuns", album + "-delete");

                return Response.ok(true).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("/{album}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("album") String album, @PathParam("password") String password) {
        if(validate(password)) {
            List<String> lst = SharedGalleryFileSystemUtilities.getPicturesFromDirectory(basePath, album);
            if(lst != null) {
                return Response.ok(lst).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("album") String album, @PathParam("picture") String picture, @PathParam("password") String password) {
        if(validate(password)) {
            byte [] data = SharedGalleryFileSystemUtilities.getDataFromPicture(basePath, album, picture);
            if(data != null)
                return Response.ok(data).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/{album}/{picture}&password={password}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("album") String album, @PathParam("picture") String picture, @PathParam("password") String password, byte [] data) {
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

                return Response.status(Response.Status.CREATED).entity(SharedGalleryFileSystemUtilities.removeExtension(new_name)).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @DELETE
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("album") String album, @PathParam("picture") String picture, @PathParam("password") String password) {
        if(validate(password)) {
            boolean created = SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
            if(created) {

                // Metadata
                String path = "/" + album + "/" + picture;
                metadata_controller.addOp(path, id, "delete");
                System.out.println("METADATA: " + metadata_controller.metadata(path));

                // Kafka
                sendToConsumers(album, picture + "-" + "delete");

                return Response.ok(true).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("/metadata&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMetadata(@PathParam("password") String password) {
        if(validate(password)) {
            List<String> current_metadata = new ArrayList<>();
            for(Metadata metadata : ((ConcurrentHashMap<String, Metadata>)metadata_controller.getMetadata()).values()) {
                String content = metadata.converted();
                current_metadata.add(content);
                System.out.println("[ SENDING ] " + content);
            }
            return Response.ok(current_metadata).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    private void sendToConsumers(String topic, String event) {
        producer.send(new ProducerRecord<>(topic, event));
        System.out.println("[ PROXY ] Sending event to consumer: " + topic + " " + event);
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
                        String picture = pictureFromMetadata(path);
                        String ext = data[4];
                        metadata_controller.addFrom(path, tmp);
                        if(tmp.getEvent().equalsIgnoreCase("delete"))
                            SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
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
                            }
                        }
                        else {
                            String picture = albumFromMetadata(path);
                            if(tmp.getEvent().equalsIgnoreCase("delete")) {
                                metadata_controller.addFrom(path, tmp);
                                SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
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

    public static void main(String[] args) throws Exception {

        id = System.nanoTime();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("PATH");
        basePath = new File("./FileServerREST" + reader.readLine());

        System.out.println("SERVER PORT");
        int port = Integer.parseInt(reader.readLine());

        System.out.println("JKS PASSWORD: ");
        char [] jks_password = reader.readLine().toCharArray();

        System.out.println("KEY PASSWORD: ");
        char [] key_password = reader.readLine().toCharArray();

        System.out.println("LOCAL PASSWORD: ");
        local_password = reader.readLine();

        basePath.mkdir();

        URI baseUri = UriBuilder.fromUri("https://" + InetAddress.getLocalHost().getHostAddress() + "/FileServerREST").port(port).build();
        ResourceConfig config = new ResourceConfig();
        config.register(SharedGalleryServerREST.class);

        SSLContext sslContext = SSLContext.getInstance("TLSv1");

        KeyStore keyStore = KeyStore.getInstance("JKS");

        try (InputStream is = new FileInputStream(KEYSTORE)) {
            keyStore.load(is, jks_password);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, key_password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        // Event dessimination - Producer

        Properties env = System.getProperties();
        Properties props = new Properties();

        props.put("zk.connect", env.getOrDefault("zk.connect", "localhost:2181/"));
        props.put("bootstrap.servers", env.getOrDefault("bootstrap.servers", "localhost:9092"));
        props.put("log.retention.ms", 5000);

        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config, sslContext);

        System.err.println("SharedGalleryServerREST: Started @ " + baseUri.toString());

        discovery = new OthersServerDiscovery(baseUri.toString(), local_password);
        new Thread(discovery).start();

        new Thread(new SharedGalleryClientDiscovery(baseUri.toString())).start();

        metadata_controller = new MetadataController(basePath);
        metadata_controller.load();

        fetchReplicaMetadata();
    }

}



