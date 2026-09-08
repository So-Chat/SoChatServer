package org.yomirein.sochatserver.persistance.sqlite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.persistance.api.DatabaseConfigurator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLiteDatabaseConfigurator extends DatabaseConfigurator<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDatabaseConfigurator.class);

    @Override
    public void initializeDatabase(String dbName) throws SQLException {
        createDatabaseDataSource(dbName);
    }

    @Override
    protected void initializeSchema(String dbName) throws SQLException {
        HikariDataSource dataSource = dataSourceFactory(dbName);
        try (Connection con = dataSource.getConnection()) {

            schemaInitializer.initialize(con);

            LOGGER.info("Created schemas successfully");
        } catch (Exception e){
            LOGGER.error("Error creating schemas", e);
            throw e;
        }
    }

    @Override
    public SQLiteSchemaInitializer createSchemaInitializer() {
        return new SQLiteSchemaInitializer();
    }


    @Override
    public HikariDataSource createDatabaseDataSource(String dbName) {
        return dataSourceFactory(dbName);
    }

    public boolean isDatabaseExists(String dbName) {
        return Files.isRegularFile(Path.of(dbName + ".db"));
    }

    public static HikariDataSource dataSourceFactory(String dbName){
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:sqlite:" + dbName + ".db");
        cfg.setMaximumPoolSize(5);

        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(10000);
        cfg.setIdleTimeout(300000);

        cfg.setPoolName("app-pool");
        cfg.addDataSourceProperty("journal_mode", "WAL");
        cfg.addDataSourceProperty("busy_timeout", "5000");
        cfg.setConnectionInitSql("PRAGMA foreign_keys = ON");

        return new HikariDataSource(cfg);
    }

}
