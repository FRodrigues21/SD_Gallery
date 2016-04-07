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

@Path("/albums")
public class SharedGalleryResource {

    private File basePath = new File("./FileServerREST");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums() {
        return Response.ok(FileSystemUtilities.getDirectoriesFromPath(basePath)).build();
    }

    @GET
    @Path("/{album}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("album") String album) {
        File dirPath = new File(basePath + "/" + album);
        return Response.ok(FileSystemUtilities.getPicturesFromDirectory(dirPath)).build();
    }

    @GET
    @Path("/{album}/{picture}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("album") String album, @PathParam("picture") String picture) {
        File imgPath = new File(basePath + "/" + album + "/" + picture);
        return Response.ok(FileSystemUtilities.getDataFromPicture(imgPath)).build();
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAlbum(String album) {
        File dirPath = new File(basePath + "/" + album);
        return Response.ok(FileSystemUtilities.createDirectory(dirPath)).build();
    }

    @DELETE
    @Path("/{album}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("album") String album) {
        File dirPath = new File(basePath + "/" + album);
        FileSystemUtilities.deleteDirectory(dirPath);
        return Response.ok().build();
    }

    @POST
    @Path("/{album}/{picture}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        File filePath = new File(basePath + "/" + album + "/" + picture);
        return Response.ok(FileSystemUtilities.createPicture(filePath, data)).build();
    }

    @DELETE
    @Path("/{album}/{picture}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("album") String album, @PathParam("picture") String picture) {
        File filePath = new File(basePath + "/" + album + "/" + picture);
        return Response.ok(FileSystemUtilities.deletePicture(filePath)).build();
    }

}
