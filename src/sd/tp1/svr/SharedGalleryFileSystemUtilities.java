package sd.tp1.svr;

import sd.tp1.gui.GalleryContentProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryFileSystemUtilities {

    /**
     *
     * @param basePath - Path of a direcoty root
     * @return a list containing the names of the directories from basePath
     */
    public static List<String> getDirectoriesFromPath(File basePath) {
        if(basePath.exists() && basePath.isDirectory())
            return Arrays.asList(basePath.listFiles()).stream().filter(f -> f.isDirectory() && !f.getName().endsWith(".deleted") && ! f.getName().startsWith(".")).map(f -> f.getName()).collect(Collectors.toList());
        return null;
    }

    /**
     *
     * @param basePath - Path of a directory root
     * @param album - Name of the directory
     * @return a list containing the names of the pictures from the directory album from basePath
     */
    public static List<String> getPicturesFromDirectory(File basePath, String album) {
        File dirPath = new File(basePath + File.separator + album);
        if(dirPath.exists() && dirPath.isDirectory())
            return Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f)).map(f -> f.getName().substring(0, f.getName().lastIndexOf('.'))).collect(Collectors.toList());
       return null;
    }

    /**
     *
     * @param basePath - Path of a root directory
     * @param album - Name of the directory
     * @param picture - Name of the picture
     * @return the data of the picture from the directory album from basePath or null
     */
    public static byte [] getDataFromPicture(File basePath, String album, String picture) {
        for(String ext : EXTENSIONS) {
            File filePath = new File(basePath + File.separator + album + File.separator + picture + "." + ext);
            if(filePath.exists() && filePath.isFile())
                return new FilePicture(filePath).getData();
        }
        return null;
    }

    /**
     * Creates a new directory with a certain name in a certain path
     * @param basePath - Path of a root directory
     * @param album - Name of the directory
     * @return the name of the directory if was created or null otherwise
     */
    public static String createDirectory(File basePath, String album) {
        File dirPath = new File(basePath + File.separator + album);
        if(dirPath.mkdir()) {
            return new FileAlbum(dirPath).getName();
        }

        return null;
    }

    /**
     * Deletes a directory with a certain name from a certain path
     * @param basePath - Path of a root directory
     * @param album - Name of the directory
     * @return true if the directory was deleted or false otherwise
     */
    public static Boolean deleteDirectory(File basePath, String album) {
        File dirPath = new File(basePath + File.separator + album);
        return dirPath.exists() && dirPath.isDirectory() && dirPath.renameTo(new File(dirPath.getAbsolutePath() + ".deleted"));
    }

    /**
     * Creates a new picture in a certain directory in a certain path
     * @param basePath - Path of a root directory
     * @param album - Name of the directory
     * @param picture - Name of the picture
     * @param data - Bytes containing the data of the picture
     * @return the name of the picture if it was created or null otherwise
     */
    public static String createPicture(File basePath, String album, String picture, byte [] data) {
        File filePath = new File(basePath + File.separator + album + File.separator + picture);
        if(!filePath.exists()) {
            try {
                System.out.println(Files.write(filePath.toPath(), data, StandardOpenOption.CREATE_NEW));
                return new FilePicture(filePath).getName();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Deletes a picture in a certain directoru in a certain path
     * @param basePath - Path of a root directory
     * @param album - Name of the directory
     * @param picture - Name of the picture
     * @return true if the pictures was deleted or false otherwise
     */
    public static Boolean deletePicture(File basePath, String album, String picture) {
        for(String ext : EXTENSIONS) {
            File filePath = new File(basePath + File.separator + album + File.separator + picture + "." + ext);
            if(filePath.exists() && filePath.isFile()) {
                filePath.delete();
                //filePath.renameTo(new File(filePath.getAbsolutePath() + ".deleted"));
                return true;
            }
        }
        return false;
    }

    // Provided by teachers
    private static class FileAlbum implements GalleryContentProvider.Album {
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

    // Provided by teachers
    private static class FilePicture implements GalleryContentProvider.Picture {
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

    public static String removeExtension(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    // Provided by teachers
    private static boolean isPicture(File f) {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        String ext = i < 0 ? "" : filename.substring(i + 1).toLowerCase();
        return f.isFile() && EXTENSIONS.contains(ext) && !filename.startsWith(".") && !filename.endsWith(".deleted");
    }

    private static final List<String> EXTENSIONS = Arrays.asList("jpeg", "png", "jpg");

}
