package sd.tp1.clt;

import org.glassfish.jersey.client.ClientConfig;
import sd.tp1.gui.GalleryContentProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RequestREST implements Request {

    private ClientConfig config = null;
    private Client client = null;
    private WebTarget target = null;

    public RequestREST(String url) {
        config = new ClientConfig();
        client = ClientBuilder.newClient(config);
        target = client.target(getBaseURI(url));
    }

    private static URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }

    @Override
    public List<String> getListOfAlbums() {
        return target.path("/albums").request().accept(MediaType.APPLICATION_JSON).get(ArrayList.class);
    }

    @Override
    public List<String> getListOfPictures(GalleryContentProvider.Album album) {
        return target.path("/albums/" + album.getName()).request().accept(MediaType.APPLICATION_JSON).get(ArrayList.class);
    }

    @Override
    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return target.path("/albums/" + album.getName() + "/" + picture.getName()).request().accept(MediaType.APPLICATION_OCTET_STREAM).get(byte[].class);
    }

    @Override
    public String createAlbum(String name) {
        return target.path("/albums/new").request().post(Entity.entity(name, MediaType.APPLICATION_JSON)).readEntity(String.class);
    }

    @Override
    public void deleteAlbum(GalleryContentProvider.Album album) {
        target.path("/albums/" + album.getName()).request().delete();
    }

    @Override
    public String uploadPicture(GalleryContentProvider.Album album, String name, byte[] data) {
        return target.path("/albums/" + album.getName() + "/" + name + "/new").request().post(Entity.entity(data, MediaType.APPLICATION_JSON)).readEntity(String.class);
    }

    @Override
    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return target.path("/albums/" + album.getName() + "/" + picture.getName()).request().delete().readEntity(Boolean.class);
    }
}
