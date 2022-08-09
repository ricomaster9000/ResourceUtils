package org.greatgamesonly.opensource.utils.resourceutils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.nio.file.Files.readString;

public final class ResourceUtils {
    private static Properties properties;

    public static String getProperty(String keyName) {
        String result = getProperties().getProperty(keyName);
        if(result == null || result.isBlank()) {
            result = System.getenv(keyName);
        }
        return result;
    }

    public static String getResourceProperty(String keyName) {
        return getProperties().getProperty(keyName);
    }

    public static Properties loadPropertiesFile() {
        Properties result = new Properties();
        try {
            result.load(getContextClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception ignore) {}
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("application.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("app.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("properties.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("properties.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            throw new RuntimeException("Unable to load any form of properties file from resource directory");
        }
        return result;
    }

    public static Properties getProperties() {
        if(properties == null || properties.isEmpty()) {
            properties = loadPropertiesFile();
        }
        return properties;
    }

    public String readFileIntoString(File file) throws IOException {
        return readString(file.toPath());
    }

    public String readFileIntoString(Path path) throws IOException {
        return readString(path);
    }

    public String readFileIntoString(URL resource) throws IOException, URISyntaxException {
        return readString(new File(resource.toURI()).toPath());
    }

    public String readFileIntoString(String path) throws IOException {
        return readString(Paths.get(path));
    }

    public static File getFileFromPath(String path) throws URISyntaxException {
        ClassLoader classLoader = getContextClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + path);
        } else {
            return new File(resource.toURI());
        }
    }

    //"*.{java,class,jar}"
    public List<File> getAllFilesByRegexPattern(String dirPath, String regexPattern) {
        return getAllFilesByRegexPattern(Paths.get(dirPath), regexPattern);
    }

    public List<File> getAllFilesByRegexPattern(Path dirPath, String regexPattern) {
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, regexPattern)) {
            for (Path entry : stream) {
                files.add(entry.toFile());
            }
            return files;
        } catch (IOException x) {
            throw new RuntimeException(String.format("error reading directory %s: %s", dirPath, x.getMessage()), x);
        }
    }

    public static List<String> getAllFileNamesInPath(String path, boolean alsoCheckSubDirectories) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (
            InputStream in = getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                if(resource.endsWith("/")) {
                    if(alsoCheckSubDirectories) {
                        filenames.addAll(getAllFileNamesInPath(resource,alsoCheckSubDirectories));
                    }
                } else {
                    filenames.add((path.endsWith("/")) ? path + resource : path + "/" + resource);
                }
            }
        }
        return filenames;
    }

    public static InputStream getResourceAsStream(String resource) {
        try (InputStream in = getContextClassLoader().getResourceAsStream(resource)) {
            return in == null ? getContextClassLoader().getResourceAsStream(resource) : in;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static Boolean doesDirectoryOrFileExistInDirectory(String directory) {
        final  URL resource = getContextClassLoader().getResource(directory);
        return (resource != null);
    }
}