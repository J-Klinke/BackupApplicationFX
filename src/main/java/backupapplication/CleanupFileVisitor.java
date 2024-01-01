package backupapplication;

import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class CleanupFileVisitor implements FileVisitor<Path> {

    private final Path targetDirectory;
    private final List<Pair<String, String>> list;

    CleanupFileVisitor(Path targetDirectory, List<Pair<String, String>> list) {
        this.targetDirectory = targetDirectory;
        this.list = list;
    }


    @Override
    public FileVisitResult preVisitDirectory(Path directoryPath, BasicFileAttributes attrs) {
        if (directoryPath == targetDirectory){
            return FileVisitResult.CONTINUE;
        }
        AtomicReference<String> originalFile = new AtomicReference<>("");
        list.forEach(stringStringPair -> {
            if (Objects.equals(stringStringPair.getValue(), directoryPath.toString())) {
                originalFile.set(stringStringPair.getKey());
            }
        });
        if (Objects.equals(originalFile.get(), "")) {
            System.out.print("Deleting " + directoryPath + "...");
            if (!FileUtil.deleteDirectory(directoryPath.toFile())) {
                System.out.println(" failed");
            } else {
                System.out.println(" done.");
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
        AtomicReference<String> originalFile = new AtomicReference<>("");
        list.forEach(stringStringPair -> {
            if (Objects.equals(stringStringPair.getValue(), filePath.toString())) {
                originalFile.set(stringStringPair.getKey());
            }
        });
        if (Objects.equals(originalFile.get(), "") && !(new File(originalFile.get()).exists())) {
            System.out.print("Deleting " + filePath + "...");
            if (filePath.toFile().delete()) {
                System.out.println(" done");
            } else {
                System.out.println(" failed");
            }

        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dirPath, IOException exc) {
        File directory = dirPath.toFile();
        if (directory.isDirectory() && directory.listFiles() == null) {
            System.out.print("Deleting " + directory + "...");
            if (directory.delete()) {
                System.out.println(" done");
            } else {
                System.out.println(" failed");
            }
        }

        return FileVisitResult.CONTINUE;
    }
}
