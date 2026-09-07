package org.yomirein.sochatserver.persistance.api;

import java.sql.Connection;

public abstract class Repository {

    protected final Connection connection;

    protected Repository(Connection connection) {
        this.connection = connection;
    }
}
