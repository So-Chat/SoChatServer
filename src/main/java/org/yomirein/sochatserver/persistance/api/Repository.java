package org.yomirein.sochatserver.persistance.api;

import com.zaxxer.hikari.HikariDataSource;

public abstract class Repository {

    protected final HikariDataSource dataSource;

    protected Repository(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
}
