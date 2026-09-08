package org.yomirein.sochatserver.persistance.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.persistance.api.DatabaseConfigurator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PostgresDatabaseConfigurator extends DatabaseConfigurator<Properties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDatabaseConfigurator.class);

    @Override
    public void initializeDatabase(Properties properties) throws SQLException {
        HikariDataSource dataSource = connectPostgres(properties);
        try (Connection con = dataSource.getConnection()) {
            Statement st = con.createStatement();

            st.executeUpdate("CREATE DATABASE  " + properties.get("db.name"));
            LOGGER.info("Created database successfully");
        } catch (Exception e){
            LOGGER.error("Error creating database", e);
            throw e;
        }
    }

    @Override
    protected void initializeSchema(Properties properties) throws SQLException {
        HikariDataSource dataSource = createDatabaseDataSource(properties);
        try (Connection con = dataSource.getConnection()) {

            schemaInitializer.initialize(con);

            LOGGER.info("Created schemas successfully");
        } catch (Exception e){
            LOGGER.error("Error creating schemas", e);
            throw e;
        }
    }

    @Override
    public PostgresSchemaInitializer createSchemaInitializer() {
        return new PostgresSchemaInitializer();
    }


    @Override
    public HikariDataSource createDatabaseDataSource(Properties properties) {
        return dataSourceFactory(
                properties.getProperty("db.url"),
                properties.getProperty("db.name"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }

    public HikariDataSource connectPostgres(Properties properties) {
        return dataSourceFactory(
                properties.getProperty("db.url"),
                "",
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }

    public boolean isDatabaseExists(Properties properties, String dbName) {
        String name;
        if (dbName == null) {
            name = properties.getProperty("db.name");
        } else {
            name = dbName;
        }
        try {
            HikariDataSource dataSource = connectPostgres(properties);

            try (Connection con = dataSource.getConnection()) {

                // Check for db with given name exists
                PreparedStatement ps =
                        con.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?");
                ps.setString(1, name);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static HikariDataSource dataSourceFactory(String ipPort, String dbName, String psqlName, String psqlPassword) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://" + ipPort + "/" + dbName);
        cfg.setUsername(psqlName);
        cfg.setPassword(psqlPassword);

        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setPoolName("app-pool");
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(cfg);
    }

}
