package org.yomirein.sochatserver.persistance.postgresql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.persistance.api.Database;
import org.yomirein.sochatserver.persistance.api.repositories.*;
import org.yomirein.sochatserver.persistance.postgresql.repositories.*;

public class PostgresDatabase extends Database{

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDatabase.class);

    public PostgresDatabase(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ChatRepository createChatRepository() {
        try (Connection connection = getConnection()) {
            return new PostgresChatRepository(connection);
        }
        catch (SQLException e) {
            LOGGER.error("Error initializing ChatRepository", e);
            return null;
        }
    }

    @Override
    public MessageRepository createMessageRepository() {
        try (Connection connection = getConnection()) {
            return new PostgresMessageRepository(connection);
        }
        catch (Exception e) {
            LOGGER.error("Error initializing MessageRepository", e);
            return null;
        }
    }

    @Override
    public UserRepository createUserRepository() {
        try (Connection connection = getConnection()) {
            return new PostgresUserRepository(connection);
        }
        catch (Exception e) {
            LOGGER.error("Error initializing UserRepository", e);
            return null;
        }
    }

    @Override
    public FriendshipRepository createFriendshipRepository() {
        try (Connection connection = getConnection()) {
            return new PostgresFriendshipRepository(connection, getUserRepository());
        }
        catch (Exception e) {
            LOGGER.error("Error initializing FriendshipRepository", e);
            return null;
        }
    }

    @Override
    public TrustKeysRepository createTrustKeysRepository() {
        try (Connection connection = getConnection()) {
            return new PostgresTrustKeysRepository(connection);
        }
        catch (Exception e) {
            LOGGER.error("Error initializing TrustKeysRepository", e);
            return null;
        }
    }

    @Override
    public MediaRepository createMediaRepository() {
        try (Connection connection = getConnection()) {
            return new PostgresMediaRepository(connection);
        }
        catch (Exception e) {
            LOGGER.error("Error initializing MediaRepository", e);
            return null;
        }

    }
}
