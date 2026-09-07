package org.yomirein.sochatserver.persistance.api;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

import org.yomirein.sochatserver.persistance.api.repositories.*;

public abstract class Database {

    protected final HikariDataSource dataSource;

    @Getter protected final ChatRepository chatRepository;
    @Getter protected final MessageRepository messageRepository;
    @Getter protected final UserRepository userRepository;
    @Getter protected final FriendshipRepository friendshipRepository;
    @Getter protected final TrustKeysRepository trustKeysRepository;
    @Getter protected final MediaRepository mediaRepository;

    protected Database() {
        this.dataSource = createDataSource();

        this.chatRepository = createChatRepository();
        this.messageRepository = createMessageRepository();
        this.userRepository = createUserRepository();
        this.friendshipRepository = createFriendshipRepository();
        this.trustKeysRepository = createTrustKeysRepository();
        this.mediaRepository = createMediaRepository();
    }

    // Creates the actual database if it does not exist.
    protected abstract void initializeDatabase();

    // Creates the DataSource used by repositories.
    protected abstract HikariDataSource createDataSource();

    // Creates the database schema.
    public abstract void initializeSchema();

    public void close() {
        dataSource.close();
    }

    public Connection getConnection() throws SQLException { return dataSource.getConnection(); }

    protected abstract ChatRepository createChatRepository();
    protected abstract MessageRepository createMessageRepository();
    protected abstract UserRepository createUserRepository();
    protected abstract FriendshipRepository createFriendshipRepository();
    protected abstract TrustKeysRepository createTrustKeysRepository();
    protected abstract MediaRepository createMediaRepository();
}
