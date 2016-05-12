package sd.tp1.svr.REST;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.sun.net.httpserver.HttpServer;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sd.tp1.svr.Imgur_Album;
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
    private static OAuth20Service service;
    private static String authorizationUrl = "";
    private static OAuth2AccessToken accessToken;
    private static String local_password;
    private static URI baseUri = null;
    private static File basePath = new File("./FileServerREST"); // Path where the server files are
    private static final File KEYSTORE = new File("./server.jks");
    /**
     * The methods from this class act the same way as the ones from REQUEST interface, but instead of null return an error status code
     */

    private static Map<String, Imgur_Album> index_albums;

    @GET
    @Path("password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums(@PathParam("password") String password) {
        if(password.equalsIgnoreCase(local_password)) {

            List<String> lst = new ArrayList<String>();

            OAuthRequest albumsReq = new OAuthRequest(Verb.GET, "https://api.imgur.com/3/account/flanmypudin/albums", service);
            service.signRequest(accessToken, albumsReq);
            com.github.scribejava.core.model.Response albumsRes = albumsReq.send();

            JSONParser parser = new JSONParser();
            JSONObject res = null;
            try {
                res = (JSONObject) parser.parse(albumsRes.getBody());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray albums = (JSONArray) res.get("data");
            Iterator albumsIt = albums.iterator();
            while (albumsIt.hasNext()) {
                JSONObject album = (JSONObject)albumsIt.next();
                String id = (String)album.get("id");
                String title = (String)album.get("title");
                if(!index_albums.containsKey(title))
                    index_albums.put(title, new Imgur_Album(id, title));
                System.out.println("FETCHED: " + title + " " + id);
                lst.add(title);
            }

            if(lst != null)
                return Response.ok(lst).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("&password={password}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAlbum(@PathParam("password") String password, String album) {
        if(password.equalsIgnoreCase(local_password)) {
            OAuthRequest albumPost = new OAuthRequest(Verb.POST, "https://api.imgur.com/3/album", service);
            albumPost.addBodyParameter("title", album);
            service.signRequest(accessToken, albumPost);
            com.github.scribejava.core.model.Response albumsRes = albumPost.send();

            JSONParser parser = new JSONParser();
            JSONObject res = null;
            try {
                res = (JSONObject) parser.parse(albumsRes.getBody());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if((boolean)res.get("success"))
                return Response.ok(album).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("password") String password, @PathParam("album") String album) {
        if(password.equalsIgnoreCase(local_password)) {
            List<String> lst = new ArrayList<String>();

            OAuthRequest albumsReq = new OAuthRequest(Verb.GET, "https://api.imgur.com/3/account/flanmypudin/album/" + index_albums.get(album).getId() + "/images", service);
            service.signRequest(accessToken, albumsReq);
            com.github.scribejava.core.model.Response albumsRes = albumsReq.send();

            JSONParser parser = new JSONParser();
            JSONObject res = null;
            try {
                res = (JSONObject) parser.parse(albumsRes.getBody());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray root = (JSONArray) res.get("data");
            Iterator picturesIt = root.iterator();
            while (picturesIt.hasNext()) {
                JSONObject picture = (JSONObject)picturesIt.next();
                String id = (String)picture.get("id");
                String title = (String)picture.get("name");
                if(index_albums.containsKey(album))
                    index_albums.get(album).addPicture(title, id);
                System.out.println("FETCHED: " + title + " " + id);
                lst.add(title);
            }

            if(lst != null)
                return Response.ok(lst).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}&password={password}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("password") String password, @PathParam("album") String album) {
        if(password.equalsIgnoreCase(local_password)) {

            OAuthRequest albumPost = new OAuthRequest(Verb.DELETE, "https://api.imgur.com/3/account/flanmypudin/album/" + index_albums.get(album).getId(), service);
            albumPost.addBodyParameter("title", album);
            service.signRequest(accessToken, albumPost);
            com.github.scribejava.core.model.Response albumsRes = albumPost.send();

            System.out.println("COD ANTES BUSCAR: " + albumsRes.getCode());

            JSONParser parser = new JSONParser();
            JSONObject res = null;
            try {
                res = (JSONObject) parser.parse(albumsRes.getBody());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            boolean deleted = (boolean)res.get("success");
            if(deleted)
                return Response.ok(deleted).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture) {
        if(password.equalsIgnoreCase(local_password)) {

            byte [] data = null;

            String endpoint = "https://api.imgur.com/3/account/flanmypudin/image/" + index_albums.get(album).getPictureId(picture);
            System.out.println(endpoint);
            OAuthRequest pictureReq = new OAuthRequest(Verb.GET, endpoint, service);
            service.signRequest(accessToken, pictureReq);
            com.github.scribejava.core.model.Response pictureRes = pictureReq.send();

            JSONParser parser = new JSONParser();
            JSONObject res = null;
            try {
                res = (JSONObject) parser.parse(pictureRes.getBody());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONObject root = (JSONObject) res.get("data");
            String link = (String)root.get("link");
            System.out.println("VOU BUSCAR" + link);
            try {
                URL image = new URL(link);
                try {
                    data = readFully(image.openStream());
                } catch (IOException e) {
                    System.out.println("ERRO URL");
                }
            } catch (MalformedURLException e) {
                System.out.println("ERRO URL");
            }
            return Response.ok(data).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
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
        return output.toByteArray();
    }

    @POST
    @Path("/{album}/{picture}&password={password}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        if(password.equalsIgnoreCase(local_password)) {

            OAuthRequest picturePost = new OAuthRequest(Verb.POST, "https://api.imgur.com/3/image", service);
            picturePost.addBodyParameter("image", Base64.getEncoder().encodeToString(data));
            picturePost.addBodyParameter("album", index_albums.get(album).getId());
            picturePost.addBodyParameter("name", picture);
            service.signRequest(accessToken, picturePost);
            com.github.scribejava.core.model.Response albumsRes = picturePost.send();

            JSONParser parser = new JSONParser();
            JSONObject res = null;
            try {
                res = (JSONObject) parser.parse(albumsRes.getBody());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            boolean created = (boolean)res.get("success");
            JSONObject root = (JSONObject) res.get("data");
            if (created) {
                String id = (String)root.get("id");
                System.out.println(id);
                index_albums.get(album).addPicture(picture, id);
                return Response.ok(picture).build();
            }

        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture) {
        if(password.equalsIgnoreCase(local_password)) {
            Boolean created = SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
            if (created)
                return Response.ok(created).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public static void main(String[] args) throws Exception {

        // TODO: Read password from console
        // pass do gestor
        // pass do certificado
        // pass do servidor
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("JKS PASSWORD: ");
        char [] jks_password = reader.readLine().toCharArray();

        System.out.println("KEY PASSWORD: ");
        char [] key_password = reader.readLine().toCharArray();

        System.out.println("LOCAL PASSWORD: ");
        local_password = reader.readLine();

        // API IMGUR CENAS

        service = new ServiceBuilder().apiKey(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
        authorizationUrl = service.getAuthorizationUrl();
        System.out.println(authorizationUrl);
        System.out.println("TOKEN: ");
        String code = reader.readLine();
        accessToken = service.getAccessToken(code);

        // FIM IMGUR API CENAS

        if(!basePath.exists())
            basePath.mkdir();

        baseUri = UriBuilder.fromUri("https://0.0.0.0/FileServerREST").port(9090).build();
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

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config, sslContext);

        System.err.println("SharedGalleryServerREST: Started @ " + baseUri.toString());

        new Thread(new SharedGalleryClientDiscovery(baseUri.toString())).start();

        index_albums = new HashMap<>();

    }

}



