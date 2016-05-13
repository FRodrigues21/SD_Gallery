package sd.tp1.svr.REST;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd.tp1.svr.SharedGalleryFileSystemUtilities;
import sd.tp1.svr.SharedGalleryClientDiscovery;

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

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
@Path("/")
public class SharedGalleryServerREST {

    private static URI baseUri = null;
    private static File basePath = new File("./FileServerREST"); // Path where the server files are
    private static String local_password;

    private static final File KEYSTORE = new File("./server.jks");

    /**
     * The methods from this class act the same way as the ones from REQUEST interface, but instead of null return an error status code
     */

    @GET
    @Path("password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums(@PathParam("password") String password) {
        if(password.equalsIgnoreCase(local_password)) {
            List<String> lst = SharedGalleryFileSystemUtilities.getDirectoriesFromPath(basePath);
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
            if(album.equalsIgnoreCase(SharedGalleryFileSystemUtilities.createDirectory(basePath, album)))
                return Response.ok(album).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("password") String password, @PathParam("album") String album) {
        if(password.equalsIgnoreCase(local_password)) {
            List<String> lst = SharedGalleryFileSystemUtilities.getPicturesFromDirectory(basePath, album);
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
            boolean created = SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
            if(created)
                return Response.ok(created).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture) {
        if(password.equalsIgnoreCase(local_password)) {
            byte [] data = SharedGalleryFileSystemUtilities.getDataFromPicture(basePath, album, picture);
            if(data != null)
                return Response.ok(data).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/{album}/{picture}&password={password}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        if(password.equalsIgnoreCase(local_password)) {
            if(picture.equalsIgnoreCase(SharedGalleryFileSystemUtilities.createPicture(basePath, album, picture, data)))
                return Response.ok(picture).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}/{picture}&password={password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("password") String password, @PathParam("album") String album, @PathParam("picture") String picture) {
        if(password.equalsIgnoreCase(local_password)) {
            Boolean created = SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
            if(created)
                return Response.ok(created).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public static void main(String[] args) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("JKS PASSWORD: ");
        char [] jks_password = reader.readLine().toCharArray();

        System.out.println("KEY PASSWORD: ");
        char [] key_password = reader.readLine().toCharArray();

        System.out.println("LOCAL PASSWORD: ");
        local_password = reader.readLine();

        if(!basePath.exists())
            basePath.mkdir();

        baseUri = UriBuilder.fromUri("https://0.0.0.0/FileServerREST").port(9090).build();
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

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config, sslContext);

        System.err.println("SharedGalleryServerREST: Started @ " + baseUri.toString());

        new Thread(new SharedGalleryClientDiscovery(baseUri.toString())).start();
    }

}



