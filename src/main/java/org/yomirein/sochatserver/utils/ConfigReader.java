package org.yomirein.sochatserver.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigReader {
    public static Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();

        try {
            String basePath = new File(
                    ConfigReader.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            Path configPath = Paths.get(basePath, "config.properties");

            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
            }

            Properties properties = new Properties();

            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            }

            for (String key : properties.stringPropertyNames()) {
                config.put(key, properties.getProperty(key));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }

        return config;
    }
}