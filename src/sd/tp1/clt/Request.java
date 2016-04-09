package sd.tp1.clt;

import sd.tp1.gui.GalleryContentProvider;

import java.util.List;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public interface Request {

    /**
     * Returns the number of failed tries to make a request to the server
     * @return number of tries
     */
    int getTries();

    /**
     * Returns the address of the server
     * @return address of the server
     */
    String getAddress();

    /**
     * Returns the names of the albums from the server
     * @return list containing the album names or null
     */
    List<String> getListOfAlbums();

    /**
     * Returns the names of the pictures from the album from the server
     * @param album - Name of the album
     * @return list containg the names of the pictures from the album or null
     */
    List<String> getListOfPictures(GalleryContentProvider.Album album);

    /**
     * Returns the bytes of a picture from an album
     * @param album - Name of the album
     * @param picture - Name of the picture
     * @return byte array containg the data from the picture or null
     */
    byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture);

    /**
     * Create an album in a server
     * @param name - Name of the album
     * @return the name of the album or null
     */
    String createAlbum(String name);

    /**
     * Delete the album in a server
     * @param album - Name of the album
     * @return true if the album exists and is deleted or false
     */
    Boolean deleteAlbum(GalleryContentProvider.Album album);

    /**
     * Upload a picture to an album in a server
     * @param album - Name of the album
     * @param name - Name of a picture
     * @param data - Data of a picture
     * @return the name of the picture or null
     */
    String uploadPicture(GalleryContentProvider.Album album, String name, byte [] data);

    /**
     * Deletes a picture of an album
     * @param album - Name of the album
     * @param picture - Name of the picture
     * @return true if the picture exists and was deleted or false if the picture doesn't exist
     */
    Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture);

}
