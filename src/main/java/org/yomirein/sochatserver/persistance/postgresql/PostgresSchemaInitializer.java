package org.yomirein.sochatserver.persistance.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.yomirein.sochatserver.persistance.api.SchemaInitializer;


public class PostgresSchemaInitializer implements SchemaInitializer {

    @Override
    public void initialize(Connection connection) throws SQLException {
        Statement st = connection.createStatement();

        initColumns(st);
        initTypes(st);
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

    public void initTypes(Statement st) throws SQLException {
        initChatRoleType(st);
        initChatType(st);
    }

    private void initChatRoleType(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TYPE chat_role AS ENUM ('MEMBER', 'ADMIN','OWNER');
        """);
    }
    private void initChatType(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TYPE chat_type AS ENUM ('PRIVATE', 'GROUP_INSECURE','GROUP_SECURE', 'CHANNEL');
        """);
    }

    private void initUsersTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE users (
                id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                nickname varchar(255),
                username varchar(255) NOT NULL UNIQUE,
                description TEXT,
                ed25519_public_key text NOT NULL,
                x25519_public_key text NOT NULL
            );
        """);
    }

    private void initFriendshipTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE friendship (
                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                user_id BIGINT NOT NULL,
                friend_id BIGINT NOT NULL,
                status VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE (user_id, friend_id)
            );
        """);
    }
    private void initTrustKeysTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE trust_keys (
                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                user_id BIGINT NOT NULL,
                fn_owner_id BIGINT NOT NULL,
                fingerprint TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """
        );
    }
    private void initChatTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE chat (
                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                type chat_type NOT NULL,
                title TEXT,

                CHECK (
                    (type = 'PRIVATE' AND title IS NULL) OR
                    (type IN ('GROUP_SECURE','GROUP_INSECURE', 'CHANNEL') AND title IS NOT NULL)
                )
            );
        """);
    }
    private void initMessageTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE message (
                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                chat_id BIGINT NOT NULL,
                sender_id BIGINT NOT NULL,
                reply_message_id BIGINT,
                content TEXT NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                message TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                key_version TEXT NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,
                FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (reply_message_id) REFERENCES message(id) ON DELETE SET NULL
            );
        """);
    }
    private void initChatParticipantsTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE chat_participants (
                chat_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                role chat_role NOT NULL,
                last_read_message_id BIGINT NOT NULL DEFAULT 0,
                PRIMARY KEY (chat_id, user_id)
            );
        """);
    }
    private void initChatSenderKeysTable(Statement st) throws SQLException {
        st.executeUpdate("""
            CREATE TABLE chat_sender_keys(
                chat_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                key_version BIGINT NOT NULL,
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
                media_id TEXT PRIMARY KEY,
                message_id BIGINT,
                sender_id BIGINT NOT NULL,
                mime_type TEXT NOT NULL,
                file_name TEXT NOT NULL,
                file_size BIGINT NOT NULL,
                width INTEGER,
                height INTEGER,
                length INTEGER,
                nonce TEXT,
                FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
           );
        """
        );
    }
}
