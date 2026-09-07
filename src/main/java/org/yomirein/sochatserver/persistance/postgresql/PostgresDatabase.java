package org.yomirein.sochatserver.persistance.postgresql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.yomirein.sochatserver.persistance.api.Database;
import org.yomirein.sochatserver.persistance.postgresql.repositories.*;

public class PostgresDatabase extends Database {
    @Override
    public void initializeDatabase() {

    }

    @Override
    public HikariDataSource createDataSource() {

    }

    @Override
    public void initializeSchema() {

    }
    @Override
    public PostgresChatRepository getChatRepository() {
        return PostgresChatRepository(getConnection());
    }

    @Override
    public PostgresMessageRepository getMessageRepository() {
        return PostgresMessageRepository(getConnection());
    }

    @Override
    public PostgresUserRepository getUserRepository() {
        return PostgresUserRepository(getConnection());
    }

    @Override
    public PostgresFriendshipRepository getFriendRepository() {
        return PostgresFriendshipRepository(getConnection());
    }

    @Override
    public PostgresTrustKeysRepository getTrustKeysRepository() {
        return PostgresTrustKeysRepository(getConnection());
    }

    @Override
    public PostgresMediaRepository getMediaRepository() {
        return PostgresMediaRepository(getConnection());
    }
}
