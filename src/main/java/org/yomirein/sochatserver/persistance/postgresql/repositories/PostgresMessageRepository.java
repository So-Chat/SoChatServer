package org.yomirein.sochatserver.persistance.postgresql.repositories;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.chats.Participant;
import org.yomirein.sochatserver.messages.Message;

import org.yomirein.sochatserver.persistance.api.repositories.MessageRepository;
import static org.yomirein.sochatserver.persistance.api.Mappers.*;

public class PostgresMessageRepository extends MessageRepository {

    public PostgresMessageRepository(HikariDataSource dataSource) {
        super(dataSource);
    }


    @Override
    public Optional<Message> findById(Long id) {
        String sql = "SELECT id, chat_id, sender_id, reply_message_id, content, timestamp, key_version FROM message WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapMessage(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> findLastById(Long id) {
        String sql = "SELECT id, chat_id, sender_id, reply_message_id, content, timestamp, key_version FROM message WHERE chat_id = ? ORDER BY timestamp DESC LIMIT 1";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapMessage(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findByChatId(Long chatId) {
        String sql = "SELECT id, chat_id, sender_id, reply_message_id, content, timestamp, key_version FROM message WHERE chat_id = ? ORDER BY timestamp ASC";
        List<Message> out = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapMessage(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteById(Long messageId) {
        String sql = "DELETE FROM message WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findTop20ByChatIdOrderByTimestampDesc(Long chatId) {
        String sql = "SELECT id, chat_id, sender_id, reply_message_id, content, timestamp, key_version FROM message WHERE chat_id = ? ORDER BY timestamp DESC LIMIT 20";
        List<Message> out = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapMessage(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findByChatIdOrderByTimestampDesc(Long chatId, int offset, int limit) {
        String sql = "SELECT id, chat_id, sender_id, reply_message_id, content, timestamp, key_version FROM message WHERE chat_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        List<Message> out = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, chatId);
            ps.setInt(3, offset);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapMessage(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findUnreadByChatIdOrderByTimestampDesc(Long chatId) {
        String sql = "SELECT COUNT(*)" +
                "FROM message AS m " +
                "JOIN chat_participants AS p ON m.chat_id = p.chat_id AND p.user_id = 56 " +
                "WHERE m.chat_id = 1 AND m.id > p.last_read_message_id " +
                "ORDER BY m.timestamp DESC";
        List<Message> out = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapMessage(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Participant setReadLastMessage(Participant participant) {
        String sql = "UPDATE chat_participants SET last_read_message_id=? WHERE chat_id = ? AND user_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, participant.getLastMessageId());
            ps.setLong(2, participant.getChatId());
            ps.setLong(3, participant.getUserId());
            ps.executeUpdate();

            return participant;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message save(Message m) {
        String sql = "INSERT INTO message(chat_id, sender_id, reply_message_id, content, timestamp, key_version) VALUES (?, ?, ?, ?, ?, ?) RETURNING id, timestamp";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, m.getChatId());
            ps.setLong(2, m.getSenderId());
            if (m.getReplyMessageId() != null) {
                ps.setLong(3, m.getReplyMessageId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setString(4, m.getContent());

            long ts = m.getTimestamp() != null
                    ? m.getTimestamp().toEpochSecond(ZoneOffset.UTC)
                    : Instant.now().getEpochSecond();

            ps.setLong(5, ts);
            ps.setInt(6, m.getKeyVersion());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.setId(rs.getLong("id"));

                    long returned = rs.getLong("timestamp");
                    m.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(returned), ZoneOffset.UTC));
                }
            }
            return m;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message update(Message m) {
        String sql = "UPDATE message SET content = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {

            ps.setString(1, m.getContent());
            ps.setLong(2, m.getId());
            ps.executeUpdate();

            return m;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
