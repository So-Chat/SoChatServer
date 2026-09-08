package org.yomirein.sochatserver.persistance.postgresql;

import java.sql.Connection;
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

            /*
            * Move set property from here and use data staight from properties instead of using dbname
            * args.properties.setProperty("db.name", args.dbName);
            */

            st.executeUpdate("CREATE DATABASE  " + properties.get("db.name"));
            LOGGER.info("Created database successfully");
        } catch (Exception e){
            LOGGER.error("Error creating database", e);
            throw e;
        }
    }

    @Override
    protected void initializeSchema(Properties properties) throws SQLException {
        HikariDataSource dataSource = connectPostgres(properties);
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
                properties.getProperty("db.username"),
                properties.getProperty("db.password"),
                properties.getProperty("db.name")
        );
    }

    public HikariDataSource connectPostgres(Properties properties) {
        return dataSourceFactory(
                properties.getProperty("db.url"),
                "",
                properties.getProperty("db.password"),
                properties.getProperty("db.name")
        );
    }

    private static HikariDataSource dataSourceFactory(String ipPort, String dbName, String psqlName, String psqlPassword) {
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
