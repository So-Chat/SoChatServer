package org.yomirein.sochatserver.chats;

import org.yomirein.sochatserver.Database;
import org.yomirein.sochatserver.users.UserRepository;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.utils.KeyParser;

import java.sql.*;
import java.util.*;

public class ChatRepository {

    private final UserRepository userRepository = new UserRepository();

    public Optional<Chat> findById(Long id) {
        String sql = "SELECT id FROM chat WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Chat chat = new Chat();
                chat.setId(rs.getLong("id"));
                chat.setParticipantsWithKeys(loadParticipants(id));
                return Optional.of(chat);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Chat> findByIdWithParticipants(Long chatId) {
        Optional<Chat> opt = findById(chatId);
        if (opt.isEmpty()) return Optional.empty();

        Chat chat = opt.get();
        chat.setParticipantsWithKeys(loadParticipants(chatId));
        return Optional.of(chat);
    }

    public List<Chat> findAllByParticipantId(Long userId) {
        String sql = "SELECT c.id " +
                "FROM chat c JOIN chat_participants cp ON c.id = cp.chat_id " +
                "WHERE cp.user_id = ?";
        List<Chat> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chat chat = new Chat();
                    chat.setId(rs.getLong("id"));
                    chat.setParticipantsWithKeys(loadParticipants(rs.getLong("id")));
                    out.add(chat);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Chat> findPrivateChatBetween(Long userId1, Long userId2) {
        String sql = """
            SELECT c.id
            FROM chat c
            JOIN chat_participants cp ON c.id = cp.chat_id
            GROUP BY c.id
            HAVING
                COUNT(*) = 2
                AND SUM(CASE WHEN cp.user_id = ? THEN 1 ELSE 0 END) > 0
                AND SUM(CASE WHEN cp.user_id = ? THEN 1 ELSE 0 END) > 0
            LIMIT 1
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId1);
            ps.setLong(2, userId2);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Chat chat = new Chat();
                chat.setId(rs.getLong("id"));
                chat.setParticipantsWithKeys(loadParticipants(chat.getId()));
                return Optional.of(chat);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<User, String> loadParticipants(Long chatId) {
        String sql = "SELECT u.id, u.username, u.ed25519_public_key, u.x25519_public_key ,cp.chat_key " +
                "FROM users u " +
                "JOIN chat_participants cp ON u.id = cp.user_id " +
                "WHERE cp.chat_id = ?";
        Map<User, String> participants = new HashMap<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEd25519PublicKey(
                            KeyParser.stringToPublicKeyED25519(rs.getString("ed25519_public_key"))
                    );
                    u.setX25519PublicKey(
                            KeyParser.stringToPublicKeyX25519(rs.getString("x25519_public_key"))
                    );

                    String chatKey = rs.getString("chat_key");
                    participants.put(u, chatKey);
                }
            }

            return participants;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addParticipant(Long chatId, Long userId, String chatKey) {
        String sql = "INSERT INTO chat_participants(chat_id, user_id, chat_key) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, userId);
            ps.setString(3, chatKey);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Chat save(Chat chat) {
        String sql = "INSERT INTO chat DEFAULT VALUES RETURNING id";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    chat.setId(rs.getLong("id"));
                }
            }

            if (chat.getParticipantsWithKeys() != null) {
                for (Map.Entry<User, String> participant : chat.getParticipantsWithKeys().entrySet()) {
                    addParticipant(chat.getId(), participant.getKey().getId(), participant.getValue());
                }
            }

            return chat;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteById(long chatId) {
        String sql = "DELETE FROM chat WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
