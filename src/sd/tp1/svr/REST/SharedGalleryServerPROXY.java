package sd.tp1.svr.REST;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sd.tp1.svr.SharedGalleryClientDiscovery;
import sd.tp1.svr.SharedGalleryFileSystemUtilities;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
@Path("/")
public class SharedGalleryServerPROXY {

    private static String apiKey = "3fdf7be0d8e5a45";
    private static String apiSecret = "936b982f9d0db99cd2edb3295cdc66d5f8c83df2";
    private static final String ACCOUNT = "flanmypudin";

    private static final int OK = 200;
    private static final int NOT_FOUND = 400;
    private static final int INTERNAL_ERROR = 500;

    private static OAuth20Service service;
    private static String authorizationUrl = "";
    private static OAuth2AccessToken accessToken;

    private static String local_password;
    private static final File KEYSTORE = new File("./server.jks");

    private static Map<String, SharedGalleryImgurAlbum> index_albums;
    private static KafkaProducer<String, String> producer;

    @GET
    @Path("password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums(@PathParam("password") String password) {
        if(validate(password)) {

            List<String> lst = new ArrayList<>();

            String endpoint = "https://api.imgur.com/3/account/" + ACCOUNT + "/albums";
            OAuthRequest albumsReq = new OAuthRequest(Verb.GET, endpoint, service);
            service.signRequest(accessToken, albumsReq);

            com.github.scribejava.core.model.Response albumsRes = albumsReq.send();

            int status = albumsRes.getCode();
            if(status == OK) {
                JSONParser parser = new JSONParser();
                JSONObject res = null;
                try {
                    res = (JSONObject) parser.parse(albumsRes.getBody());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                JSONArray albums = (JSONArray) res.get("data");
                for (Object obj : albums) {
                    JSONObject album = (JSONObject)obj;
                    String id = (String)album.get("id");
                    String title = (String)album.get("title");
                    if(title != null && !index_albums.containsKey(title))
                        index_albums.put(title, new SharedGalleryImgurAlbum(id, title));
                    else if(title == null || (index_albums.containsKey(title) && !index_albums.get(title).getId().equalsIgnoreCase(id))) {
                        title = title + "_" + id;
                        if(!index_albums.containsKey(title))
                            index_albums.put(title, new SharedGalleryImgurAlbum(id, title));
                    }
                }

                if(index_albums.keySet().size() > 0) {
                    lst.addAll(index_albums.keySet());
                    return Response.ok(lst).build();
                }
            }
            else if(status == INTERNAL_ERROR)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Yes IMGUR return this... but whyyyy?!?!
            else
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

            System.out.println("[ PROXY ] Creating album: " + album);

            String endpoint = "https://api.imgur.com/3/album";
            OAuthRequest albumPost = new OAuthRequest(Verb.POST, endpoint, service);
            albumPost.addBodyParameter("title", album);
            service.signRequest(accessToken, albumPost);

            com.github.scribejava.core.model.Response albumRes = albumPost.send();

            int status = albumRes.getCode();
            if(status == OK) {
                JSONParser parser = new JSONParser();
                JSONObject res = null;
                try {
                    res = (JSONObject) parser.parse(albumRes.getBody());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if ((boolean) res.get("success")) {
                    JSONObject root = (JSONObject)res.get("data");
                    String title = album;
                    String id = (String) root.get("id");
                    if(index_albums.containsKey(album))
                        title = album + "_" + id;
                    index_albums.put(title, new SharedGalleryImgurAlbum(id, title));
                    sendToConsumers("Albuns", album + "-create");
                    System.out.println(" [ PROXY ] Creating album " + album);
                    return Response.status(Response.Status.CREATED).entity(title).build();
                }
            }
            else if(status == INTERNAL_ERROR)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Yes IMGUR return this... but whyyyy?!?!
            else
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
            if(index_albums.containsKey(album)) {

                String endpoint = "https://api.imgur.com/3/album/" + index_albums.get(album).getId();
                OAuthRequest albumDel = new OAuthRequest(Verb.DELETE, endpoint, service);
                albumDel.addBodyParameter("title", album);
                service.signRequest(accessToken, albumDel);

                com.github.scribejava.core.model.Response albumRes = albumDel.send();

                int status = albumRes.getCode();
                if(status == OK) {
                    index_albums.remove(album);
                    sendToConsumers("Albuns", album + "-delete");
                    System.out.println("[ PROXY ] Deleting album: " + album);
                    return Response.ok(true).build();
                }
                else if(status == INTERNAL_ERROR)
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Yes IMGUR return this... but whyyyy?!?!
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
            if(index_albums.containsKey(album)) {

                List<String> lst = new ArrayList<>();

                String endpoint = "https://api.imgur.com/3/album/" + index_albums.get(album).getId() + "/images";

                OAuthRequest picturesReq = new OAuthRequest(Verb.GET, endpoint, service);
                service.signRequest(accessToken, picturesReq);

                com.github.scribejava.core.model.Response picturesRes = picturesReq.send();

                int status = picturesRes.getCode();
                if(status == OK) {
                    JSONParser parser = new JSONParser();
                    JSONObject res = null;
                    try {
                        res = (JSONObject) parser.parse(picturesRes.getBody());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    JSONArray root = (JSONArray) res.get("data");
                    for (Object obj : root) {
                        JSONObject picture = (JSONObject)obj;
                        String id = (String)picture.get("id");
                        String title = (String)picture.get("name");
                        if(title != null && (title.contains(".jpg") || title.contains(".jpeg") || title.contains(".png")))
                            title = title.substring(0, title.lastIndexOf('.'));
                        if(title != null && !index_albums.get(album).hasPicture(title))
                            index_albums.get(album).addPicture(title, id);
                        else if(title == null || (index_albums.get(album).hasPicture(title) && !index_albums.get(album).getPictureId(title).equalsIgnoreCase(id))) {
                            title = title + "_" + id;
                            if(!index_albums.get(album).hasPicture(title))
                                index_albums.get(album).addPicture(title, id);
                        }
                    }
                    if(!index_albums.get(album).getPictures().isEmpty()) {
                        lst.addAll(index_albums.get(album).getPictures().keySet());
                        return Response.ok(lst).build();
                    }
                }
                else if(status == INTERNAL_ERROR)
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Yes IMGUR return this... but whyyyy?!?!
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

            if(index_albums.containsKey(album) &&  index_albums.get(album).hasPicture(picture)) {

                System.out.println("[ PROXY ] Picture was found now fetching");

                OAuthRequest pictureGet = new OAuthRequest(Verb.GET, "https://api.imgur.com/3/image/" + index_albums.get(album).getPictureId(picture), service);
                service.signRequest(accessToken, pictureGet);

                com.github.scribejava.core.model.Response pictureRes = pictureGet.send();

                int status = pictureRes.getCode();
                if(status == OK) {
                    byte [] data = null;
                    JSONParser parser = new JSONParser();
                    JSONObject res = null;
                    try {
                        res = (JSONObject) parser.parse(pictureRes.getBody());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    JSONObject root = (JSONObject) res.get("data");
                    String link = (String)root.get("link");
                    try {
                        URL image = new URL(link);
                        try {
                            data = readFully(image.openStream());
                        } catch (IOException e) {
                            System.out.println("ERRO NA CRIAÇÃO DO URL");
                        }
                    } catch (MalformedURLException e) {
                        System.out.println("ERRO NA CRIAÇÃO DO URL");
                    }
                    return Response.ok(data).build();
                }
                else if(status == INTERNAL_ERROR)
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Yes IMGUR return this... but whyyyy?!?!
            }
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
            if(index_albums.containsKey(album)) {

                System.out.println(" [ PROXY ] Creating picture " + picture);

                String endpoint = "https://api.imgur.com/3/image";
                OAuthRequest picturePost = new OAuthRequest(Verb.POST, endpoint, service);
                picturePost.addBodyParameter("image", Base64.getEncoder().encodeToString(data));
                picturePost.addBodyParameter("album", index_albums.get(album).getId());
                picturePost.addBodyParameter("name", picture);

                service.signRequest(accessToken, picturePost);
                com.github.scribejava.core.model.Response pictureRes = picturePost.send();

                int status = pictureRes.getCode();
                if(status == OK) {
                    JSONParser parser = new JSONParser();
                    JSONObject res = null;
                    try {
                        res = (JSONObject) parser.parse(pictureRes.getBody());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    boolean created = (boolean)res.get("success");
                    JSONObject root = (JSONObject) res.get("data");
                    if (created) {
                        System.out.println("[ PROXY ] Uploaded picture: " + picture + " successfully");
                        String id = (String)root.get("id");
                        String title = picture;
                        if(index_albums.get(album).hasPicture(title))
                            title = picture + "_" + id;
                        if(title != null && (title.contains(".jpg") || title.contains(".jpeg") || title.contains(".png")))
                            title = SharedGalleryFileSystemUtilities.removeExtension(title);
                        index_albums.get(album).addPicture(title, id);
                        sendToConsumers(album, title + "-" + "create");
                        return Response.status(Response.Status.CREATED).entity(title).build();
                    }
                }
                else if(status == INTERNAL_ERROR)
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @DELETE
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture) {
        if(validate(password)) {
            if(index_albums.containsKey(album) && index_albums.get(album).hasPicture(picture)) {

                System.out.println(" [ PROXY ] Deleting picture " + picture);

                String endpoint = "https://api.imgur.com/3/image/" + index_albums.get(album).getPictureId(picture);
                OAuthRequest picturePost = new OAuthRequest(Verb.DELETE, endpoint, service);

                service.signRequest(accessToken, picturePost);
                com.github.scribejava.core.model.Response pictureRes = picturePost.send();

                int status = pictureRes.getCode();
                if(status == OK) {
                    index_albums.get(album).removePicture(picture);
                    sendToConsumers(album, picture + "-" + "delete");
                    return Response.ok(true).build();
                }
                else if(status == INTERNAL_ERROR)
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // Yes IMGUR return this... but whyyyy?!?!
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    private void sendToConsumers(String topic, String event) {
        System.out.println("[ PROXY ] Sending event to consumer: " + topic + " " + event);
        producer.send(new ProducerRecord<>(topic, event));
    }

    private static boolean validate(String password) {
        return password.equalsIgnoreCase(local_password);
    }

    private static byte[] readFully(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        output.close();
        input.close();
        return output.toByteArray();
    }

    public static void main(String[] args) throws Exception {

        index_albums = new TreeMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("KAFKA IP: ");
        String kafka_ip = reader.readLine();

        System.out.println("SERVER PORT: ");
        int port = Integer.parseInt(reader.readLine());

        System.out.println("JKS PASSWORD: ");
        char [] jks_password = reader.readLine().toCharArray();

        System.out.println("KEY PASSWORD: ");
        char [] key_password = reader.readLine().toCharArray();

        System.out.println("LOCAL PASSWORD: ");
        local_password = reader.readLine();

        service = new ServiceBuilder().apiKey(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
        authorizationUrl = service.getAuthorizationUrl();
        System.out.println(authorizationUrl);
        System.out.println("TOKEN: ");
        String code = reader.readLine();
        accessToken = service.getAccessToken(code);

        URI baseUri = UriBuilder.fromUri("https://" + InetAddress.getLocalHost().getHostAddress() + "/FileServerRESTPROXY").port(port).build();
        ResourceConfig config = new ResourceConfig();
        config.register(SharedGalleryServerPROXY.class);

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

        props.put("zk.connect", env.getOrDefault("zk.connect", kafka_ip+":2181/"));
        props.put("bootstrap.servers", env.getOrDefault("bootstrap.servers", kafka_ip+":9092"));
        props.put("log.retention.ms", 1000);

        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config, sslContext);

        System.err.println("SharedGalleryServerREST: Started @ " + baseUri.toString());

        new Thread(new SharedGalleryClientDiscovery(baseUri.toString())).start();
    }

}



