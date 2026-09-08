package org.yomirein.sochatserver.persistance.postgresql;

import com.zaxxer.hikari.HikariDataSource;

import org.yomirein.sochatserver.persistance.api.Database;
import org.yomirein.sochatserver.persistance.api.repositories.*;
import org.yomirein.sochatserver.persistance.postgresql.repositories.*;

public class PostgresDatabase extends Database{

    public PostgresDatabase(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ChatRepository createChatRepository() {
        return new PostgresChatRepository(getDataSource());
    }

    @Override
    public MessageRepository createMessageRepository() {
        return new PostgresMessageRepository(getDataSource());
    }

    @Override
    public UserRepository createUserRepository() {
        return new PostgresUserRepository(getDataSource());
    }

    @Override
    public FriendshipRepository createFriendshipRepository() {
        return new PostgresFriendshipRepository(getDataSource(), getUserRepository());
    }

    @Override
    public TrustKeysRepository createTrustKeysRepository() {
        return new PostgresTrustKeysRepository(getDataSource());
    }

    @Override
    public MediaRepository createMediaRepository() {
        return new PostgresMediaRepository(getDataSource());

    }
}
