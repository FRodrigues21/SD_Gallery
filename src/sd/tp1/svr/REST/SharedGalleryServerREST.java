package sd.tp1.svr.REST;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd.tp1.svr.FileSystemUtilities;
import sd.tp1.svr.SharedGalleryClientDiscovery;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;

@Path("/")
public class SharedGalleryServerREST {

    private File basePath = new File("./FileServerREST");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums() {
        List<String> lst = FileSystemUtilities.getDirectoriesFromPath(basePath);
        if(lst != null)
            return Response.ok(lst).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAlbum(String album) {
        if(album.equalsIgnoreCase(FileSystemUtilities.createDirectory(basePath, album)))
            return Response.ok(album).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("album") String album) {
        List<String> lst = FileSystemUtilities.getPicturesFromDirectory(basePath, album);
        if(lst != null)
            return Response.ok(lst).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("album") String album) {
        FileSystemUtilities.deleteDirectory(basePath, album);
        return Response.ok().build();
    }

    @GET
    @Path("/{album}/{picture}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("album") String album, @PathParam("picture") String picture) {
        byte [] data = FileSystemUtilities.getDataFromPicture(basePath, album, picture);
        if(data != null)
            return Response.ok(data).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/{album}/{picture}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        if(picture.equalsIgnoreCase(FileSystemUtilities.createPicture(basePath, album, picture, data)))
            return Response.ok(true).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}/{picture}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("album") String album, @PathParam("picture") String picture) {
        Boolean created = FileSystemUtilities.deletePicture(basePath, album, picture);
        if(created)
            return Response.ok(created).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public static void main(String[] args) throws Exception {

        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8090" + "/FileServerREST/";
        System.err.println("FileServerREST: Started @ " + address_s);
        URI baseUri = UriBuilder.fromUri("http://" + InetAddress.getLocalHost().getCanonicalHostName() + "/FileServerREST/").port(8090).build();

        ResourceConfig config = new ResourceConfig();

        config.register(SharedGalleryServerREST.class);

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        System.err.println("REST Server ready... ");

        // Receives
        Thread r = new Thread(new SharedGalleryClientDiscovery(address_s));
        r.start();
    }

}



