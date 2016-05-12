package sd.tp1.svr.REST;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd.tp1.svr.SharedGalleryFileSystemUtilities;
import sd.tp1.svr.SharedGalleryClientDiscovery;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.*;
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

    /**
     * The methods from this class act the same way as the ones from REQUEST interface, but instead of null return an error status code
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums() {
        List<String> lst = SharedGalleryFileSystemUtilities.getDirectoriesFromPath(basePath);
        if(lst != null)
            return Response.ok(lst).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAlbum(String album) {
        if(album.equalsIgnoreCase(SharedGalleryFileSystemUtilities.createDirectory(basePath, album)))
            return Response.ok(album).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("album") String album) {
        List<String> lst = SharedGalleryFileSystemUtilities.getPicturesFromDirectory(basePath, album);
        if(lst != null)
            return Response.ok(lst).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("album") String album) {
        boolean created = SharedGalleryFileSystemUtilities.deleteDirectory(basePath, album);
        if(created)
            return Response.ok(created).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}/{picture}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("album") String album, @PathParam("picture") String picture) {
        byte [] data = SharedGalleryFileSystemUtilities.getDataFromPicture(basePath, album, picture);
        if(data != null)
            return Response.ok(data).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/{album}/{picture}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        if(picture.equalsIgnoreCase(SharedGalleryFileSystemUtilities.createPicture(basePath, album, picture, data)))
            return Response.ok(picture).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}/{picture}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("album") String album, @PathParam("picture") String picture) {
        Boolean created = SharedGalleryFileSystemUtilities.deletePicture(basePath, album, picture);
        if(created)
            return Response.ok(created).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }



    public static void main(String[] args) throws Exception {

        if(!basePath.exists())
            basePath.mkdir();

        // Get local address and create server address
        // Using getHostAddress because the ipv4 doesn't work on mac (at least mine)
        //baseUri = UriBuilder.fromUri("http://" + InetAddress.getLocalHost().getHostAddress() + "/FileServerREST").port(8090).build();
        baseUri = UriBuilder.fromUri("http://0.0.0.0/FileServerREST").port(9090).build();
        ResourceConfig config = new ResourceConfig();
        config.register(SharedGalleryServerREST.class);

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        System.err.println("SharedGalleryServerREST: Started @ " + baseUri.toString());

        // Receives
        Thread r = new Thread(new SharedGalleryClientDiscovery(baseUri.toString()));
        r.start();
    }

}



