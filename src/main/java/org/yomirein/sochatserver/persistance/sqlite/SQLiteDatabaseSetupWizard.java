package org.yomirein.sochatserver.persistance.sqlite;

import java.util.Properties;

import org.yomirein.sochatserver.persistance.api.DatabaseSetupWizard;
import org.yomirein.sochatserver.utils.ConfigReader;

public class SQLiteDatabaseSetupWizard implements DatabaseSetupWizard {

    private static final SQLiteDatabaseConfigurator configurator = new SQLiteDatabaseConfigurator();

    @Override
    public SQLiteDatabase setupDatabase() {
        try {
            if (!configurator.isDatabaseExists("sochat")) {
                configurator.initializeDatabase("sochat");
                configurator.initializeSchema("sochat");

                Properties properties = new Properties();
                properties.setProperty("db.type", "sqlite");

                ConfigReader.saveConfig(properties);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup database", e);
        }

        return new SQLiteDatabase(configurator.createDatabaseDataSource("sochat"));
    }
}
