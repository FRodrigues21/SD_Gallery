package sd.tp1.svr.REST;

import sd.tp1.svr.FileSystemUtilities;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

@Path("/albums")
public class SharedGalleryResource {

    private File basePath = new File("./FileServerREST");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums() {
        List<String> lst = FileSystemUtilities.getDirectoriesFromPath(basePath);
        if(lst != null)
            return Response.ok(lst).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{album}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("album") String album) {
        return Response.ok(FileSystemUtilities.getPicturesFromDirectory(basePath, album)).build();
    }

    @GET
    @Path("/{album}/{picture}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("album") String album, @PathParam("picture") String picture) {
        return Response.ok(FileSystemUtilities.getDataFromPicture(basePath, album, picture)).build();
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAlbum(String album) {
        return Response.ok(FileSystemUtilities.createDirectory(basePath, album)).build();
    }

    @DELETE
    @Path("/{album}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("album") String album) {
        FileSystemUtilities.deleteDirectory(basePath, album);
        return Response.ok().build();
    }

    @POST
    @Path("/{album}/{picture}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        return Response.ok(FileSystemUtilities.createPicture(basePath, album, picture, data)).build();
    }

    @DELETE
    @Path("/{album}/{picture}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("album") String album, @PathParam("picture") String picture) {
        return Response.ok(FileSystemUtilities.deletePicture(basePath, album, picture)).build();
    }

}
