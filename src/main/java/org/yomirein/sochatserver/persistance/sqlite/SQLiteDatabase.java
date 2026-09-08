package org.yomirein.sochatserver.persistance.sqlite;

import com.zaxxer.hikari.HikariDataSource;

import org.yomirein.sochatserver.persistance.api.Database;
import org.yomirein.sochatserver.persistance.api.repositories.*;
import org.yomirein.sochatserver.persistance.sqlite.repositories.*;

public class SQLiteDatabase extends Database{

    public SQLiteDatabase(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ChatRepository createChatRepository() {
        return new SQLiteChatRepository(getDataSource());
    }

    @Override
    public MessageRepository createMessageRepository() {
        return new SQLiteMessageRepository(getDataSource());
    }

    @Override
    public UserRepository createUserRepository() {
        return new SQLiteUserRepository(getDataSource());
    }

    @Override
    public FriendshipRepository createFriendshipRepository() {
        return new SQLiteFriendshipRepository(getDataSource(), getUserRepository());
    }

    @Override
    public TrustKeysRepository createTrustKeysRepository() {
        return new SQLiteTrustKeysRepository(getDataSource());
    }

    @Override
    public MediaRepository createMediaRepository() {
        return new SQLiteMediaRepository(getDataSource());

    }
}
