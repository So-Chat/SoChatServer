package org.yomirein.sochatserver.media;

import org.yomirein.sochatserver.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


public class MediaRepository {
    public Optional<Media> findById(String id) {
        String sql = "SELECT * FROM media WHERE media_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(sql)) {

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
                            rs.getObject("width", Integer.class),
                            rs.getObject("height", Integer.class),
                            rs.getObject("length", Integer.class)
                    );
                    return Optional.of(media);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteById(String id) {
        String sql = "DELETE FROM media WHERE media_id = ?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(Media media) {
        String sql = "UPDATE media SET message_id = ?, width = ?, height = ?, length = ?, WHERE media_id = ?";

        try (Connection c = Database.getConnection()) {
            try (PreparedStatement psUpdate = c.prepareStatement(sql)) {
                psUpdate.setLong(1, media.getMessageId());
                psUpdate.setInt(2, media.getWidth());
                psUpdate.setInt(3, media.getHeight());
                psUpdate.setInt(4, media.getLength());

                return psUpdate.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Media save(String mediaId, long userId, String mimeType, String fileName, long fileSize) {
        String sql = "INSERT INTO media(media_id, sender_id, mime_type, file_name, file_size) VALUES(?,?,?,?,?)";

        try (Connection c = Database.getConnection()) {
            try (PreparedStatement psInsert = c.prepareStatement(sql)) {
                psInsert.setString(1, mediaId);
                psInsert.setLong(2, userId);
                psInsert.setString(3, mimeType);
                psInsert.setString(4, fileName);
                psInsert.setLong(5, fileSize);

                psInsert.executeUpdate();
            }
            return new Media(
                    mediaId, null, userId,
                    mimeType, fileName,
                    fileSize,
                    null, null, null);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
