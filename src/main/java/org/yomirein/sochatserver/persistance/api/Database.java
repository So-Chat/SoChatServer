package org.yomirein.sochatserver.persistance.api;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import org.yomirein.sochatserver.persistance.api.repositories.*;

public abstract class Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    protected final HikariDataSource dataSource;

    @Getter protected final ChatRepository chatRepository;
    @Getter protected final MessageRepository messageRepository;
    @Getter protected final UserRepository userRepository;
    @Getter protected final FriendshipRepository friendshipRepository;
    @Getter protected final TrustKeysRepository trustKeysRepository;
    @Getter protected final MediaRepository mediaRepository;

    protected Database(HikariDataSource dataSource) {
        this.dataSource = dataSource;

        try {
            this.chatRepository = createChatRepository();
            this.messageRepository = createMessageRepository();
            this.userRepository = createUserRepository();
            this.friendshipRepository = createFriendshipRepository();
            this.trustKeysRepository = createTrustKeysRepository();
            this.mediaRepository = createMediaRepository();


        } catch (SQLException e) {
            LOGGER.error("Error while initalizing database");
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    public void close() {
        dataSource.close();
    }

    public Connection getConnection() throws SQLException { return dataSource.getConnection(); }

    protected abstract ChatRepository createChatRepository() throws SQLException;
    protected abstract MessageRepository createMessageRepository() throws SQLException;
    protected abstract UserRepository createUserRepository() throws SQLException;
    protected abstract FriendshipRepository createFriendshipRepository() throws SQLException;
    protected abstract TrustKeysRepository createTrustKeysRepository() throws SQLException;
    protected abstract MediaRepository createMediaRepository() throws SQLException;

}
