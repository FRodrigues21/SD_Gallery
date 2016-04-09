package sd.tp1.clt;

import org.glassfish.jersey.client.ClientConfig;
import sd.tp1.gui.GalleryContentProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class RequestREST implements Request {

    private ClientConfig config = null;
    private Client client = null;
    private WebTarget target = null;
    private String url;
    private int tries;

    private static final int OK = Response.Status.OK.getStatusCode();

    public RequestREST(String url) {
        this.tries = 0;
        this.url = url;
        config = new ClientConfig();
        client = ClientBuilder.newClient(config);
        target = client.target(getBaseURI(url));
    }

    private static URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }

    public int getTries() {
        return tries++;
    }

    public String getAddress() {
        return url;
    }

    @Override
    public List<String> getListOfAlbums() {
        Response response = target.request().accept(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == OK)
            return response.readEntity(ArrayList.class);
        return null;
    }

    @Override
    public List<String> getListOfPictures(GalleryContentProvider.Album album) {
        Response response = target.path(album.getName()).request().accept(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == OK)
            return response.readEntity(ArrayList.class);
        return null;
    }

    @Override
    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        Response response = target.path(album.getName() + "/" + picture.getName()).request().accept(MediaType.APPLICATION_OCTET_STREAM).get();
        if(response.getStatus() == OK)
            return response.readEntity(byte[].class);
        return null;
    }

    @Override
    public String createAlbum(String name) {
        Response response = target.request().post(Entity.entity(name, MediaType.APPLICATION_JSON));
        if(response.getStatus() == OK)
            return response.readEntity(String.class);
        return null;
    }

    @Override
    public Boolean deleteAlbum(GalleryContentProvider.Album album) {
        Response response = target.path(album.getName()).request().delete();
        if(response.getStatus() == OK)
            return response.readEntity(Boolean.class);
        return false;
    }

    @Override
    public String uploadPicture(GalleryContentProvider.Album album, String name, byte[] data) {
        Response response = target.path(album.getName() + "/" + name).request().post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
        if(response.getStatus() == OK)
            return response.readEntity(String.class);
        return null;
    }

    @Override
    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        Response response = target.path(album.getName() + "/" + picture.getName()).request().delete();
        if(response.getStatus() == OK)
            return response.readEntity(Boolean.class);
        return false;
    }
}
