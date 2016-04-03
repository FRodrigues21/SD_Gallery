package sd.tp1.clt;

import sd.tp1.gui.GalleryContentProvider;

/**
 * Created by franciscorodrigues on 03/04/16.
 */
public class SharedPicture implements GalleryContentProvider.Picture  {

    final String name;

    public SharedPicture(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
