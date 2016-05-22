package sd.tp1.svr;

import sd.tp1.gui.GalleryContentProvider;

import java.util.List;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public class SyncSOAP implements Sync {

    private String url;
    private String password;

    public SyncSOAP(String url, String password) {
        this.url = url;
        this.password = password;
    }

    @Override
    public List<String> sync() {
        return null;
    }

    @Override
    public int getTries() {
        return 0;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public List<String> getListOfAlbums() {
        return null;
    }

    @Override
    public List<String> getListOfPictures(GalleryContentProvider.Album album) {
        return null;
    }

    @Override
    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return new byte[0];
    }

    @Override
    public String createAlbum(String name) {
        return null;
    }

    @Override
    public Boolean deleteAlbum(GalleryContentProvider.Album album) {
        return null;
    }

    @Override
    public String uploadPicture(GalleryContentProvider.Album album, String name, byte[] data) {
        return null;
    }

    @Override
    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return null;
    }
}
