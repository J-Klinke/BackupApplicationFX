package backupapplication;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class FileUtil {

    public static boolean compareFiles(File a, File b) throws IOException, NoSuchAlgorithmException {
        return Objects.equals(generateHash(a), generateHash(b));
    }

    public static BigInteger generateHash(File file) {
        byte[] hash;
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            hash = MessageDigest.getInstance("MD5").digest(data);

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algorithm problems.");
            return null;
        } catch (IOException e) {
            System.err.println("Hash generation not possible for file with name" + file.getName());
            return null;

        }
        return new BigInteger(1, hash);
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static File sanitizeFile(File fileToSanitize) {
        // todo: Müsste man sich mal genauer anschauen und diverse Edge-Cases betrachten, was ist mit meheren Punkten,
        //  was ist mit nur Punkten, was ist mit Dateien mit nur verbotenen Namen, die im Anschluss keinen Namen mehr haben?
        String sanitizedFileName = fileToSanitize.getName().replace("[\\\\/:*?\"<>|]", "");
        Path path = fileToSanitize.toPath();
        return new File(path.resolve(sanitizedFileName).toString());
    }

}
