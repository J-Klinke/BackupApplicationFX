package backupapplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class CleanupFileVisitor implements FileVisitor<Path> {

    private Path sourceDirectory;
    private Path targetDirectory;

    CleanupFileVisitor(Path sourceDirectory, Path targetDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
    }


    @Override
    public FileVisitResult preVisitDirectory(Path directoryPath, BasicFileAttributes attrs) {

        Path relativize = targetDirectory.relativize(directoryPath);
        Path fileInSource = sourceDirectory.resolve(relativize);
        if (!fileInSource.toFile().exists()) {
            System.out.print("Deleting " + directoryPath + "...");
            FileUtil.deleteDirectory(directoryPath.toFile());
            System.out.println(" done.");
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {

        Path relativize = targetDirectory.relativize(filePath);
        Path fileInSource = sourceDirectory.resolve(relativize);
        if (!fileInSource.toFile().exists()) {
            System.out.print("Deleting " + filePath + "...");
            filePath.toFile().delete();
            System.out.println(" done");
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dirPath, IOException exc) {
        File directory = dirPath.toFile();
        if (directory.isDirectory() && directory.listFiles() == null){
            directory.delete();
        }

        return FileVisitResult.CONTINUE;
    }
}
