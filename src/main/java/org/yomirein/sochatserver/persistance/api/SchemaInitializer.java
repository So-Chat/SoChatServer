package org.yomirein.sochatserver.persistance.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface SchemaInitializer {
    void initialize(Connection connection) throws SQLException ;
}
