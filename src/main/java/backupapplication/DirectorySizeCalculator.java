package backupapplication;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectorySizeCalculator implements FileVisitor<Path> {
    private long directorySize = 0;

    /**
     * calculates the total apparent size of the given directory.
     * @param directory the given directory
     * @param directorySizeCalculator the matching FileVisitor
     * @return the total apparent size
     */
    public long calculateSize(Path directory, DirectorySizeCalculator directorySizeCalculator) {
        directorySize = 0;
        try {
            Files.walkFileTree(directory, directorySizeCalculator);
        } catch (IOException e) {
            System.err.println("IOE Exception thrown in Files.walkFileTree");
            directorySize = -1;
        }
        return directorySize;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        directorySize += dir.toFile().length();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        directorySize += attrs.size();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
}
