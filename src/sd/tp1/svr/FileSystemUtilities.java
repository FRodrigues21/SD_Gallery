package sd.tp1.svr;

import sd.tp1.gui.GalleryContentProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemUtilities {

    public static List<String> getDirectoriesFromPath(File basePath) {
        if(basePath.exists())
            return Arrays.asList(basePath.listFiles()).stream().filter(f -> f.isDirectory() && ! f.getName().endsWith(".deleted") && ! f.getName().startsWith(".")).map(f -> new String(f.getName())).collect(Collectors.toList());
        return null;
    }

    public static List<String> getPicturesFromDirectory(File basePath, String album) {
        File dirPath = new File(basePath + "/" + album);
       if(dirPath.exists())
           return Arrays.asList(dirPath.listFiles()).stream().filter(f -> isPicture(f)).map(f -> new String(f.getName())).collect(Collectors.toList());
       return null;
    }

    public static byte [] getDataFromPicture(File basePath, String album, String picture) {
        File imgPath = new File(basePath + "/" + album + "/" + picture);
        if(imgPath.exists())
            return new FilePicture(imgPath).getData();
        return null;
    }

    public static String createDirectory(File basePath, String album) {
        File dirPath = new File(basePath + "/" + album);
        if(!dirPath.exists()) {
            dirPath.mkdir();
            return new FileAlbum(dirPath).getName();
        }
        return null;
    }

    public static void deleteDirectory(File basePath, String album) {
        File dirPath = new File(basePath + "/" + album);
        if(dirPath.exists())
            dirPath.renameTo(new File(dirPath.getAbsolutePath() + ".deleted"));
    }

    public static String createPicture(File basePath, String album, String picture, byte [] data) {
        File filePath = new File(basePath + "/" + album + "/" + picture);
        if(!filePath.exists()) {
            try {
                Files.write(filePath.toPath(), data, StandardOpenOption.CREATE_NEW);
                return new FilePicture(filePath).getName();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public static Boolean deletePicture(File basePath, String album, String picture) {
        File filePath = new File(basePath + "/" + album + "/" + picture);
        if(filePath.exists()) {
            filePath.renameTo(new File(filePath.getAbsolutePath() + ".deleted"));
            return true;
        }
        return false;
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
