package atypon.app.node.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


// these are CRUD operations of the files and their directory (Synchronized)?
public class FileOperations {
    public static void deleteDirectory(String directory) throws IOException {
        Path directoryToDelete = Paths.get(directory);
        Files.walk(directoryToDelete)
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    public static void writeJsonAtLocation(String json, String directory, String name) {
        File file = new File(directory, name);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean isDirectoryExists(String directory) {
        File file = new File(directory);
        return file.isDirectory() && file.exists();
    }
    public static boolean isFileExists(String directory) {
        File file = new File(directory);
        return file.isFile() && file.exists();
    }
    public static void createDirectory(String path, String name) {
        File collectionDir = new File(path + "/" + name);
        collectionDir.mkdirs();
    }
    public static List<String> readDirectories(String path) {
        File parentDirectory = new File(path);
        File[] subDirectories = parentDirectory.listFiles(file -> file.isDirectory());
        List<String> directoriesList = new ArrayList<>();
        if (subDirectories != null) {
            for (File subDirectory : subDirectories) {
                directoriesList.add(subDirectory.getName());
            }
        }
        return directoriesList;
    }
    public static void deleteFile(String filePath) throws IOException {
        Files.delete(Paths.get(filePath));
    }
    public static String readFileAsString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }
    public static void updateDirectoryName(String path, String oldName, String newName) {
        File originalFile = new File(path + "/" + oldName);
        File renamedFile = new File( path + "/" + newName);
        originalFile.renameTo(renamedFile);
    }
}
