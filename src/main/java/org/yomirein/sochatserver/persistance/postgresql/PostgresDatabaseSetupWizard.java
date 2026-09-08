package org.yomirein.sochatserver.persistance.postgresql;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.persistance.api.DatabaseSetupWizard;
import org.yomirein.sochatserver.utils.ConfigReader;
import org.yomirein.sochatserver.utils.InputReader;

public final class PostgresDatabaseSetupWizard implements DatabaseSetupWizard {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDatabaseSetupWizard.class);
    private static final PostgresDatabaseConfigurator configurator = new PostgresDatabaseConfigurator();

    @Override
    public PostgresDatabase setupDatabase() {
        Properties properties = databaseCheck();

        return new PostgresDatabase(configurator.createDatabaseDataSource(properties));
    }


    // Checking for existing 'sochat' database
    private static Properties databaseCheck() {
        // Firstly getting config to get all data
        Map<String, String> propertiesMap = ConfigReader.getConfig();
        try {
            ConfigReader.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // If config has url trying to connect to server
        if (propertiesMap.containsKey("db.url")) {
            LOGGER.info("Config already contains Database info, skipping setup...");
            return propertiesMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldVal, newVal) -> newVal,
                    Properties::new
                ));
        } else {
            LOGGER.info("Config has no Database info, starting setup...");
            Properties properties = new Properties();

            properties.setProperty("db.type", "postgresql");

            readDbInput(properties);
            ConfigReader.saveConfig(properties);

            if (configurator.isDatabaseExists(properties, null)) {
                LOGGER.info("Given database already exists");
                return properties;
            } else {
                try {
                    configurator.initializeDatabase(properties);
                    configurator.initializeSchema(properties);
                } catch (Exception e) {
                    LOGGER.error("Error setting up database", e);
                    System.exit(1);
                }
            }

            return properties;
        }
    }

    // Getting postgres authorization, like ip, username and password
    private static void readDbInput(Properties input) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Type server Ip:Port (localhost:5432): ");
        input.setProperty("db.url", InputReader.readLine(in, "localhost:5432"));

        System.out.println("Type psql root username (postgres): ");
        input.setProperty("db.username", InputReader.readLine(in, "postgres"));

        System.out.println("Type psql root password: ");
        input.setProperty("db.password", InputReader.readLine(in, ""));

        System.out.println("Type database name(default: sochat): ");
        input.setProperty("db.name", InputReader.readLine(in, "sochat"));
    }


}
