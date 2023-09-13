package backupapplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class BackupApplication extends Observable {
    private File sourceRootFile;
    private long sourceDirectorySize = 0;
    /**
     * progressSize is initialized at 4MB since the root directory's size is included in sourceDirectorySize, but not
     * parsed in backup()
     */
    private long progressSize = 4096;
    private File targetRootFile;

    private BackupMode backupMode;
    private DirectorySizeCalculator directorySizeCalculator = new DirectorySizeCalculator();


    public BackupApplication(File sourceRootFile, File targetRootFile) {
        this.sourceRootFile = sourceRootFile;
        this.targetRootFile = targetRootFile;
    }

    /**
     * method for executing the 'newBackup' mode
     */
    public void newBackup(String newDirectoryName) {
        File backupDirectory = this.targetRootFile.toPath().resolve(newDirectoryName).toFile();
        backupDirectory.mkdir();
        backup(this.sourceRootFile, backupDirectory);
    }

    /**
     * method for executing the 'consecutiveBackup' mode
     */
    public void consecutiveBackup() {
        backup(this.sourceRootFile, this.targetRootFile);
    }

    /**
     * method for executing the 'updatedBackup' mode
     */
    public void updatedBackup()  {
        backup(this.sourceRootFile, this.targetRootFile);
        cleanUp(this.sourceRootFile, this.targetRootFile);
    }

    /**
     * this method executes the basic backup. All the files will be copied using copySingleFile().
     * In it, progressSize is updated
     *
     * @param sourceFile source Directory
     * @param targetFile target Directory
     */
    public void backup(File sourceFile, File targetFile) {
        if (sourceFile.listFiles().length == 0) {
            return;
        }
        for (File file : sourceFile.listFiles()) {
            File newEntry = new File(targetFile.toPath().resolve(Path.of(file.getName(), "")).toUri());
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
                    targetFile.delete();
                }
                if (!targetFile.exists()) {
                    targetFile.mkdir();
                }
                newEntry.mkdir();
                backup(file, newEntry);
            }
        }
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
                targetDirectory.createNewFile();
            }
            if (!FileUtil.compareFiles(file, targetDirectory)) {
                System.out.print("Copying " + file.getPath() + "...");
                InputStream in = new FileInputStream((file));
                OutputStream out = new FileOutputStream(targetDirectory);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                System.out.println(" done.");
            }
        } catch (IOException e) {
            targetDirectory.delete();
            System.out.println(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * this method cleans the target directory for the 'updated backup' mode. To achieve this, the method compares the
     * target directory (after the backup) with the source Directory and deletes everything not found in source
     *
     * @param targetDirectory the target Directory
     * @param sourceDirectory the source Directory
     */
    public void cleanUp(File sourceDirectory, File targetDirectory){
        try {
            Files.walkFileTree(targetDirectory.toPath(), new CleanupFileVisitor(sourceDirectory.toPath(), targetDirectory.toPath()));
        } catch (Exception e) {
            System.out.println(e);
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
