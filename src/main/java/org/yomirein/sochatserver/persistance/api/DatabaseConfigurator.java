package org.yomirein.sochatserver.persistance.api;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public abstract class DatabaseConfigurator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfigurator.class);

    protected final SchemaInitializer schemaInitializer;

    protected DatabaseConfigurator() {
        try {
            this.schemaInitializer = createSchemaInitializer();
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize database schema", e);
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    // Creates the database schema.
    protected abstract void initializeSchema(T args) throws SQLException;

    // Creates the actual database if it does not exist.
    protected abstract void initializeDatabase(T args) throws SQLException;

    // Creates the DataSource used by repositories.
    protected abstract HikariDataSource createDatabaseDataSource(T args);

    protected abstract SchemaInitializer createSchemaInitializer() throws SQLException;

}
