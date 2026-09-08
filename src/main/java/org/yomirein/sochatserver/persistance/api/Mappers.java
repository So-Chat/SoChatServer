package org.yomirein.sochatserver.persistance.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import org.yomirein.sochatserver.friendship.Friendship;
import org.yomirein.sochatserver.friendship.FriendshipStatus;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.messages.Message;
import org.yomirein.sochatserver.persistance.api.repositories.UserRepository;
import org.yomirein.sochatserver.utils.KeyParser;

public class Mappers {
    public static User mapUser(ResultSet rs) throws SQLException {
        User u = null;
        try {

            u = new User(
                    rs.getInt("id"),
                    rs.getString("nickname"),
                    rs.getString("username"),
                    rs.getString("description"),
                    KeyParser.stringToPublicKeyED25519(rs.getString("ed25519_public_key")),
                    KeyParser.stringToPublicKeyX25519(rs.getString("x25519_public_key"))
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }

    public static Message mapMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getLong("id"));
        m.setChatId(rs.getLong("chat_id"));
        m.setSenderId(rs.getLong("sender_id"));

        m.setReplyMessageId(rs.getLong("reply_message_id"));
        m.setKeyVersion(rs.getInt("key_version"));

        m.setContent(rs.getString("content"));
        long timestamp = rs.getLong("timestamp");
        Timestamp ts = Timestamp.from(Instant.ofEpochSecond(timestamp));
        if (ts != null) m.setTimestamp(ts.toLocalDateTime());
        return m;
    }

    public static Friendship mapFriendship(ResultSet rs, UserRepository userRepository) throws SQLException {
        Friendship f = new Friendship();
        f.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        long fid = rs.getLong("friend_id");
        userRepository.findById(uid).ifPresent(f::setUser);
        userRepository.findById(fid).ifPresent(f::setFriend);
        f.setStatus(FriendshipStatus.valueOf(rs.getString("status")));
        return f;
    }
}
