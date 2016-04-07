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

    public static List<String> getPicturesFromDirectory(File basePath) {
       if(basePath.exists())
           return Arrays.asList(basePath.listFiles()).stream().filter(f -> isPicture(f)).map(f -> new String(f.getName())).collect(Collectors.toList());
       return null;
    }

    public static byte [] getDataFromPicture(File basePath) {
        if(basePath.exists())
            return new FilePicture(basePath).getData();
        return new byte[1];
    }

    public static String createDirectory(File basePath) {
        if(!basePath.exists()) {
            basePath.mkdir();
            return new FileAlbum(basePath).getName();
        }
        return null;
    }

    public static void deleteDirectory(File basePath) {
        if(basePath.exists())
            basePath.renameTo(new File(basePath.getAbsolutePath() + ".deleted"));
    }

    public static String createPicture(File basePath, byte [] data) {
        if(!basePath.exists()) {
            try {
                Files.write(basePath.toPath(), data, StandardOpenOption.CREATE_NEW);
                return new FilePicture(basePath).getName();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public static Boolean deletePicture(File basePath) {
        if(basePath.exists()) {
            basePath.renameTo(new File(basePath.getAbsolutePath() + ".deleted"));
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
