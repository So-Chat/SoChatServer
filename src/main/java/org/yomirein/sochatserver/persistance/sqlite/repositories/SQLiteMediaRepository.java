package org.yomirein.sochatserver.persistance.sqlite.repositories;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.yomirein.sochatserver.media.Media;

import org.yomirein.sochatserver.persistance.api.repositories.MediaRepository;

public class SQLiteMediaRepository extends MediaRepository {

    public SQLiteMediaRepository(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Optional<Media> findById(String id) {
        String sql = "SELECT * FROM media WHERE media_id = ?";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement psSelect = connection.prepareStatement(sql)) {

            psSelect.setString(1, id);

            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                else {
                    Media media = new Media(
                            rs.getString("media_id"),
                            rs.getLong("message_id"),
                            rs.getLong("sender_id"),
                            rs.getString("mime_type"),
                            rs.getString("file_name"),
                            rs.getLong("file_size"),
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getInt("length"),
                            rs.getString("nonce")
                    );
                    return Optional.of(media);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM media WHERE media_id = ?";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteOrphaned() {
        String sql = """
            DELETE FROM media
            WHERE media_id IN (
                SELECT md.media_id
                FROM media md
                WHERE md.message_id IS NOT NULL
                AND NOT EXISTS (
                    SELECT 1
                    FROM message m
                    WHERE m.id = md.message_id
                )
                AND NOT EXISTS (
                    SELECT 1
                    FROM users u
                    WHERE md.media_id = u.avatar_media_id
                )
                LIMIT 1000
            );
        """;
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> checkForNonexistentIOIds(List<String> idList) {
        if (idList == null || idList.isEmpty()) {
            return List.of();
        }

        Set<String> existingIds = new HashSet<>(idList.size());
        int batchSize = 500;

        try (Connection connection = dataSource.getConnection()) {
            for (int i = 0; i < idList.size(); i += batchSize) {
                int end = Math.min(idList.size(), i + batchSize);
                List<String> batch = idList.subList(i, end);

                String placeholders = String.join(", ", Collections.nCopies(batch.size(), "?"));
                String sql = "SELECT id FROM media WHERE id IN (" + placeholders + ")";

                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    for (int j = 0; j < batch.size(); j++) {
                        ps.setString(j + 1, batch.get(j));
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            existingIds.add(rs.getString("id"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while checking media IDs", e);
        }

        if (existingIds.size() == idList.size()) {
            return List.of();
        }
        return idList.stream()
            .filter(id -> !existingIds.contains(id))
            .toList();
    }

    @Override
    public List<Media> findAttachedMessage(long messageId) {
        String sql = "SELECT * FROM media WHERE message_id = ?";

        List<Media> out = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Media media = new Media(rs.getString("media_id"),
                            rs.getLong("message_id"),
                            rs.getLong("sender_id"),
                            rs.getString("mime_type"),
                            rs.getString("file_name"),
                            rs.getLong("file_size"),
                            rs.getObject("width", Integer.class),
                            rs.getObject("height", Integer.class),
                            rs.getObject("length", Integer.class),
                            rs.getString("nonce")
                    );

                    out.add(media);
                }
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(String mediaId, Long message_id, Integer width, Integer height, Integer length) {
        String sql = "UPDATE media SET message_id = COALESCE(?, message_id), width = COALESCE(?, width), height = COALESCE(?, height), length = COALESCE(?, length) WHERE media_id = ?";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement psUpdate = connection.prepareStatement(sql)) {
            psUpdate.setObject(1, message_id);
            psUpdate.setObject(2, width);
            psUpdate.setObject(3, height);
            psUpdate.setObject(4, length);
            psUpdate.setObject(5, mediaId);

            return psUpdate.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Media save(String mediaId, long userId, String mimeType, String fileName, long fileSize, String nonce) {
        String sql = "INSERT INTO media(media_id, sender_id, mime_type, file_name, file_size, nonce) VALUES(?,?,?,?,?,?)";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement psInsert = connection.prepareStatement(sql)) {
            psInsert.setString(1, mediaId);
            psInsert.setLong(2, userId);
            psInsert.setString(3, mimeType);
            psInsert.setString(4, fileName);
            psInsert.setLong(5, fileSize);
            psInsert.setString(6, nonce);

            psInsert.executeUpdate();
            return new Media(
                    mediaId, null, userId,
                    mimeType, fileName,
                    fileSize,
                    null, null, null, nonce);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
