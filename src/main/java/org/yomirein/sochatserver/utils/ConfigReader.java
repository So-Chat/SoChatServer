package org.yomirein.sochatserver.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


// Config reader
// For now it uses only for database
public class ConfigReader {

    // It has only method for getting config

    public static Map<String, String> getConfig() {
        // Create config Map<String, String> that will contain our config data
        Map<String, String> config = new HashMap<>();

        // Getting our built server .jar file location
        try {
            new File(
                    ConfigReader.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            // Getting config.properties location
            Path configPath = Paths.get("config.properties");

            // If it not exists create it
            if (Files.exists(configPath)) {
                if (Files.isDirectory(configPath)) {
                    throw new RuntimeException("config.properties is a directory, expected file");
                }
            } else {
                Files.createFile(configPath);
            }

            Properties properties = new Properties();

            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            }
            // Get data from config
            for (String key : properties.stringPropertyNames()) {
                config.put(key, properties.getProperty(key));
            }

        }
        catch (IOException | RuntimeException | URISyntaxException e) {
            throw new RuntimeException("Failed to load config", e);
        }

        return config;
    }

    public static void saveConfig(Properties properties) {
        try (OutputStream out = new FileOutputStream("config.properties")) {
            properties.store(out, "");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
