package org.jekajops.util;

import java.io.File;

public class FileManager {
    public static File getFromResources(String name) {
        var str = String.format("%s/src/main/resources/%s", System.getProperty("user.dir"), name);
        return new File(str);
    }
}
