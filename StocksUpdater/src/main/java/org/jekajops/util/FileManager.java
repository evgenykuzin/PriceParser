package org.jekajops.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class FileManager {
    public static File getFromResources(String name) {
        var str = String.format("%s/src/main/resources/%s", System.getProperty("user.dir"), name);
        return new File(str);
    }

    public static File download(String urlString, String fileName, String suffix) throws IOException {
        Path path = Files.createTempFile(Paths.get(getFromResources("").getAbsolutePath()), fileName, suffix);
        return download(urlString, path.toFile());
    }

    public static File download(String urlString, File file) throws IOException {
        URL url = new URL(urlString);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        return file;
    }

    public static List<String> readFile(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void writeToFile(File file, String text) {
        try {
            Files.writeString(file.toPath(), text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
