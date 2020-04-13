package nl.underkoen.browniebot.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Created by Under_Koen on 02/03/2018.
 */
public class FileUtil {
    public static File getRunningDir() {
        try {
            return new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String getAllContent(File file) throws FileNotFoundException {
        return getAllContent(new FileInputStream(file));
    }

    public static String getAllContent(InputStream inputStream) {
        StringBuilder result = new StringBuilder("");

        try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static void writeContent(File file, String content) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
        fileWriter.flush();
        fileWriter.close();
    }
}