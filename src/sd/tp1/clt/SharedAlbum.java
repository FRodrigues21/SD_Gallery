package sd.tp1.clt;

import sd.tp1.gui.GalleryContentProvider;

/**
 * Created by franciscorodrigues on 03/04/16.
 */
public class SharedAlbum implements GalleryContentProvider.Album {

    final String name;

    public SharedAlbum(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
