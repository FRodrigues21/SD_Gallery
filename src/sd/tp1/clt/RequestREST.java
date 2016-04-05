package sd.tp1.clt;

import org.glassfish.jersey.client.ClientConfig;
import sd.tp1.gui.GalleryContentProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franciscorodrigues on 05/04/16.
 */
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
    public List<GalleryContentProvider.Album> getListOfAlbums() {
        List<GalleryContentProvider.Album> lst = new ArrayList<>();
        List<String> tmp = target.path("/albums").request().accept(MediaType.APPLICATION_JSON).get(ArrayList.class);
        if(tmp == null)
            return null;
        for (String s: tmp) {
            lst.add(new SharedAlbum(s));
        }
        return lst;
    }

    @Override
    public List<GalleryContentProvider.Picture> getListOfPictures(GalleryContentProvider.Album album) {
        List<GalleryContentProvider.Picture> lst = new ArrayList<>();
        List<String> tmp = target.path("/albums/" + album.getName()).request().accept(MediaType.APPLICATION_JSON).get(ArrayList.class);
        if(tmp == null)
            return null;
        for (String s: tmp) {
            lst.add(new SharedPicture(s));
        }
        return lst;
    }

    @Override
    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return target.path("/albums/" + album.getName() + "/" + picture.getName()).request().accept(MediaType.APPLICATION_OCTET_STREAM).get(byte[].class);
    }

    @Override
    public GalleryContentProvider.Album createAlbum(String name) {
        return null;
    }

    @Override
    public void deleteAlbum(GalleryContentProvider.Album album) {

    }

    @Override
    public GalleryContentProvider.Picture uploadPicture(GalleryContentProvider.Album album, String name, byte[] data) {
        return null;
    }

    @Override
    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return null;
    }
}
