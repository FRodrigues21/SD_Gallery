package sd.tp1.svr.REST;

import sd.tp1.gui.GalleryContentProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/albums")
public class SharedGalleryResource {

    private File basePath = new File("./FileServerREST");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfAlbums() {
        System.err.printf("getListOfAlbums()\n");
        List<String> tmp = new ArrayList<>();
        List<FileAlbum> albums = Arrays.asList(basePath.listFiles()).stream().filter(f -> f.isDirectory() && ! f.getName().endsWith(".deleted") && ! f.getName().startsWith(".")).map(f -> new FileAlbum(f)).collect(Collectors.toList());
        for (FileAlbum a : albums) {
            tmp.add(a.getName());
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("/{album}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfPictures(@PathParam("album") String album) {
        File dirPath = new File(basePath + "/" + album);
        if(dirPath.exists()) {
            List<FilePicture> pictures = Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f)).map(f -> new FilePicture(f)).collect(Collectors.toList());
            List<String> tmp = new ArrayList<>();
            for(FilePicture p: pictures){
                tmp.add(p.getName());
            }
            return Response.ok(tmp).build();
        }
        return null;
    }

    @GET
    @Path("/{album}/{picture}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPictureData(@PathParam("album") String album, @PathParam("picture") String picture) {
        File dirPath = new File(basePath + "/" + album);
        if(dirPath.exists()) {
            List<FilePicture> pictures = Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f) && f.getName().equalsIgnoreCase(picture)).map(f -> new FilePicture(f)).collect(Collectors.toList());
            return Response.ok(pictures.get(0).getData()).build();
        }
        return null;
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAlbum(String album) {
        File dirPath = new File(basePath + "/" + album);
        if(!dirPath.exists()) {
            dirPath.mkdir();
            return Response.ok(album).build();
        }
        return Response.ok(null).build();
    }

    @DELETE
    @Path("/{album}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAlbum(@PathParam("album") String album) {
        File dirPath = new File(basePath + "/" + album);
        if(dirPath.exists()) {
            dirPath.renameTo(new File(dirPath.getAbsolutePath() + ".deleted"));
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @Path("/{album}/{picture}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadPicture(@PathParam("album") String album, @PathParam("picture") String picture, byte [] data) {
        File filePath = new File(basePath + "/" + album + "/" + picture);
        if(!filePath.exists()) {
            try {
                Files.write(filePath.toPath(), data, StandardOpenOption.CREATE_NEW);
                return Response.ok(picture).build();
            } catch (IOException e) {
                return null;
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{album}/{picture}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deletePicture(@PathParam("album") String album, @PathParam("picture") String picture) {
        File filePath = new File(basePath + "/" + album + "/" + picture);
        if(filePath.exists()) {
            filePath.renameTo(new File(filePath.getAbsolutePath() + ".deleted"));
            return Response.ok(true).build();
        }
        return Response.ok(false).build();
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

        public String toString() {
            return String.format("name: %s", getName());
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
