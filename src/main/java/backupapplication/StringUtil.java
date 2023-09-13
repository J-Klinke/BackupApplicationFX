package backupapplication;

import java.nio.file.Path;

class StringUtil {

    /**
     * reformats the given path so it fits in the textBox
     *
     * @param path the given path
     * @return the formatted String
     */
    public static String printPath(Path path) {
        String[] splitPath = path.toString().split("(?<=/)");
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder eachLine = new StringBuilder();
        int i = 0;
        while (i < splitPath.length) {
            while (i < splitPath.length && (splitPath[i].length() > 45 || eachLine.length() + splitPath[i].length() < 45)) {
                eachLine.append(splitPath[i]);
                i++;
            }
            stringBuilder.append(eachLine).append("\n");
            eachLine.setLength(0);
        }
        return stringBuilder.toString();
    }

}