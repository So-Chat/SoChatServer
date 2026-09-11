package org.yomirein.sochatserver.persistance.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.yomirein.sochatserver.persistance.api.SchemaInitializer;

public class SQLiteSchemaInitializer implements SchemaInitializer {
    @Override
    public void initialize(Connection connection) throws SQLException {
        System.out.println("SQLite schema initialization started");

        try (Statement st = connection.createStatement()) {
            initColumns(st);
        }

        System.out.println("SQLite schema initialization finished");
    }

    public void initColumns(Statement st) throws SQLException {
        initUsersTable(st);
        initFriendshipTable(st);
        initTrustKeysTable(st);
        initChatTable(st);
        initMessageTable(st);
        initChatParticipantsTable(st);
        initChatSenderKeysTable(st);
        initMediaTable(st);

    }

    private void initUsersTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY,
                avatar_media_id UUID NULL REFERENCES media(media_id),
                nickname TEXT CHECK (length(nickname) <= 255),
                username TEXT NOT NULL UNIQUE CHECK (length(username) <= 255),
                description TEXT CHECK (length(description) <= 255),
                ed25519_public_key TEXT NOT NULL,
                x25519_public_key TEXT NOT NULL
            );
        """);
    }

    private void initFriendshipTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE friendship (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                friend_id INTEGER NOT NULL,
                status TEXT NOT NULL CHECK (length(status) <= 255),
                created_at INTEGER NOT NULL DEFAULT (unixepoch()),
                updated_at INTEGER NOT NULL DEFAULT (unixepoch()),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE (user_id, friend_id)
            );
        """);
    }
    private void initTrustKeysTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE trust_keys (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                fn_owner_id INTEGER NOT NULL,
                fingerprint TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """
        );
    }
    private void initChatTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE chat (
                id INTEGER PRIMARY KEY,
                type TEXT NOT NULL
                    CHECK (
                        type IN (
                            'PRIVATE',
                            'GROUP_INSECURE',
                            'GROUP_SECURE',
                            'CHANNEL'
                        )
                    ),
                title TEXT,


                CHECK (
                    (type = 'PRIVATE' AND title IS NULL)
                    OR
                    (
                        type IN (
                            'GROUP_SECURE',
                            'GROUP_INSECURE',
                            'CHANNEL'
                        )
                        AND title IS NOT NULL
                    )
                )
            );
        """);
    }
    private void initMessageTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE message (
                id INTEGER PRIMARY KEY,
                chat_id INTEGER NOT NULL,
                sender_id INTEGER NOT NULL,
                reply_message_id INTEGER,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL DEFAULT (unixepoch()),

                key_version TEXT NOT NULL,

            );
        """);
        /*
            FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,
            FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (reply_message_id) REFERENCES message(id) ON DELETE SET NULL
        */
    }
    private void initChatParticipantsTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE chat_participants (
                chat_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                role TEXT NOT NULL
                    CHECK (
                        role IN (
                            'MEMBER',
                            'ADMIN',
                            'OWNER'
                        )
                    ),
                last_read_message_id INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (chat_id, user_id),
                FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);
    }
    private void initChatSenderKeysTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE chat_sender_keys(
                chat_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                key_version INTEGER NOT NULL,
                chat_key TEXT NOT NULL,
                PRIMARY KEY (chat_id, user_id, key_version),
                FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);
    }
    private void initMediaTable(Statement st) throws SQLException {
        st.executeUpdate("""
           CREATE TABLE media (
                media_id TEXT PRIMARY KEY NOT NULL,
                message_id INTEGER,
                sender_id INTEGER NOT NULL,
                mime_type TEXT NOT NULL,
                file_name TEXT NOT NULL,
                file_size INTEGER NOT NULL,
                width INTEGER,
                height INTEGER,
                length INTEGER,
                nonce TEXT,
                FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (message_id) REFERENCES message(id) ON DELETE CASCADE
           );
        """
        );
    }
}
