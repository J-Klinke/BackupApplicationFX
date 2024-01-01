package backupapplication;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BackupApplication extends Observable {
    private File sourceRootFile;
    private long sourceDirectorySize = 0;
    /**
     * progressSize is initialized at 4MB since the root directory's size is included in sourceDirectorySize, but not
     * parsed in backup()
     */
    private long progressSize = 4096;
    private File targetRootFile;
    private final DirectorySizeCalculator directorySizeCalculator = new DirectorySizeCalculator();


    public BackupApplication(File sourceRootFile, File targetRootFile) {
        this.sourceRootFile = sourceRootFile;
        this.targetRootFile = targetRootFile;
    }

    /**
     * method for executing the 'newBackup' mode
     */
    public void newBackup(String newDirectoryName) {
        File backupDirectory = this.targetRootFile.toPath().resolve(newDirectoryName).toFile();
        if (!backupDirectory.mkdir()) {
            System.out.println(backupDirectory.getPath() + " could not be created.");
        }
        List<Pair<String, String>> list = new ArrayList<>();
        backup(this.sourceRootFile, backupDirectory, list);
    }

    /**
     * method for executing the 'consecutiveBackup' mode
     */
    public void consecutiveBackup() {
        List<Pair<String, String>> list = new ArrayList<>();
        backup(this.sourceRootFile, this.targetRootFile, list);
    }

    /**
     * method for executing the 'updatedBackup' mode
     */
    public void updatedBackup() {
        List<Pair<String, String>> list = new ArrayList<>();
        list = backup(this.sourceRootFile, this.targetRootFile, list);
        cleanUp(this.targetRootFile, list);
    }

    /**
     * this method executes the basic backup. All the files will be copied using copySingleFile().
     * In it, progressSize is updated
     *
     * @param sourceFile source Directory
     * @param targetFile target Directory
     */
    public List<Pair<String, String>> backup(File sourceFile, File targetFile, List<Pair<String, String>> list) {
        if (Objects.equals(Objects.requireNonNull(sourceFile.listFiles()).length, 0)) {
            return list;
        }
        for (File file : Objects.requireNonNull(sourceFile.listFiles())) {
            File newEntry = new File(targetFile.toPath().resolve(Path.of(FileUtil.sanitizeFile(file).getName(), "")).toUri());
            list.add(new Pair<>(file.getPath(), newEntry.getPath()));
            if (!file.isDirectory()) {
                copySingleFile(file, newEntry);
                try {
                    progressSize += Files.size(file.toPath());
                    notifyObserver(this);
                } catch (IOException e) {
                    System.err.println("IOEException while trying to read the size of file.");
                }

            } else {
                progressSize += file.length();
                notifyObserver(this);
                if (!targetFile.isDirectory()) {
                    if (!targetFile.delete()) {
                        System.out.println(targetFile.getPath() + " couldn't be deleted");
                    }
                }
                if (!targetFile.exists()) {
                    if (!targetFile.mkdir()) {
                        System.out.println(targetFile.getPath() + " couldn't be created");
                    }
                }
                if (!newEntry.mkdir()) {
                    System.out.println(newEntry.getPath() + " couldn't be created");
                }
                backup(file, newEntry, list);
            }
        }
        return list;
    }

    /**
     * this method copies a single file.
     * If an identical file already exists in the targetDirectory, it will not be copied
     *
     * @param file            th file to be copied
     * @param targetDirectory the directory the files is copied to
     */
    private void copySingleFile(File file, File targetDirectory) {
        try {
            if (!targetDirectory.exists()) {
                if (!targetDirectory.createNewFile()) {
                    System.out.println(targetDirectory.getPath() + " couldn't be created");
                }
            }
            if (!FileUtil.compareFiles(file, targetDirectory)) {
                System.out.print("Copying " + file.getPath() + "...");
                try (FileInputStream in = new FileInputStream((file));
                     FileOutputStream out = new FileOutputStream(targetDirectory)
                ) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                System.out.println(" done.");
            }
        } catch (IOException e) {
            if (targetDirectory.delete()) {
                System.out.println(targetDirectory.getPath() + " couldn't be deleted");
            }
            e.printStackTrace(System.out);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * this method cleans the target directory for the 'updated backup' mode. To achieve this, the method compares the
     * target directory (after the backup) with the source Directory and deletes everything not found in source
     *
     * @param targetDirectory the target Directory
     */
    public void cleanUp(File targetDirectory, List<Pair<String, String>> list) {
        try {
            Files.walkFileTree(targetDirectory.toPath(), new CleanupFileVisitor(targetDirectory.toPath(), list));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    public File getSourceRootFile() {
        return sourceRootFile;
    }

    public File getTargetRootFile() {
        return targetRootFile;
    }

    public void setSourceRootFile(File sourceRootFile) {
        this.sourceRootFile = sourceRootFile;
    }

    public void setTargetRootFile(File targetRootFile) {
        this.targetRootFile = targetRootFile;
    }

    public long getSourceDirectorySize() {
        return sourceDirectorySize;
    }

    public void setSourceDirectorySize(long sourceDirectorySize) {
        this.sourceDirectorySize = sourceDirectorySize;
    }

    public long getProgressSize() {
        return progressSize;
    }

    public void setProgressSize(long progressSize) {
        this.progressSize = progressSize;
    }

    public DirectorySizeCalculator getDirectorySizeCalculator() {
        return directorySizeCalculator;
    }
}
