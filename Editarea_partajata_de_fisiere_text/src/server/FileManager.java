package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {

    private static final String DIRECTORY = "server_files";
    private static final Map<String, String> lockedFiles = new ConcurrentHashMap<>();

    public static List<String> getFiles() {
        List<String> list = new ArrayList<>();
        File folder = new File(DIRECTORY);

        if (!folder.exists()) folder.mkdir();

        File[] files = folder.listFiles();
        if (files == null) return list;

        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".txt")) {
                list.add(f.getName());
            }
        }
        return list;
    }

    public static File getFile(String name) {
        return new File(DIRECTORY + "/" + name);
    }

    public static boolean isLocked(String file) {
        return lockedFiles.containsKey(file);
    }

    public static String lockedBy(String file) {
        return lockedFiles.get(file);
    }

    public static void lock(String file, String user) {
        lockedFiles.put(file, user);
    }

    public static void unlock(String file) {
        lockedFiles.remove(file);
    }

    public static void unlockAllForUser(String user) {
        lockedFiles.entrySet().removeIf(e -> e.getValue().equals(user));
    }
}