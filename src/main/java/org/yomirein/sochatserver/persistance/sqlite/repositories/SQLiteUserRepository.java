package org.yomirein.sochatserver.persistance.sqlite.repositories;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.persistance.api.repositories.UserRepository;
import static org.yomirein.sochatserver.persistance.api.Mappers.*;

public class SQLiteUserRepository extends UserRepository {

    public SQLiteUserRepository(HikariDataSource dataSource) {
        super(dataSource);
    }

    private static final String USER_FIELDS =
        "id, nickname, username, description, ed25519_public_key, x25519_public_key";

    @Override
    public User saveUser(User user) {
        String sql =
            "INSERT INTO users(nickname, username, ed25519_public_key, x25519_public_key) " +
            "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getNickname());
            ps.setString(2, user.getUsername());
            ps.setString(
                3,
                Base64.getEncoder().encodeToString(
                    user.getEd25519PublicKey().getEncoded()
                )
            );
            ps.setString(
                4,
                Base64.getEncoder().encodeToString(
                    user.getX25519PublicKey().getEncoded()
                )
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Optional<User> findByName(String username) {
        String sql = "SELECT " + USER_FIELDS + " FROM users WHERE username = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username);
            return executeUserQuery(ps);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT " + USER_FIELDS + " FROM users WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            return executeUserQuery(ps);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> searchByUsername(String username, int offset, int limit) {
        List<User> out = new ArrayList<>();

        if (username == null || username.isEmpty()) {
            return out;
        }

        String sql =
            "SELECT " + USER_FIELDS +
            " FROM users " +
            "WHERE username LIKE ? COLLATE NOCASE " +
            "ORDER BY id DESC " +
            "LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username + "%");
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapUser(rs));
                }
            }

            return out;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateUser(
        Long id,
        String username,
        String nickname,
        String description,
        String avatarId
    ) {
        String sql =
            "UPDATE users SET username = COALESCE(?, username), nickname = ?, description = ?, avatar_media_id = ?, WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, nickname);
                ps.setString(3, avatarId);
                ps.setString(4, description);
                ps.setLong(5, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))  {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<User> executeUserQuery(PreparedStatement ps)
        throws SQLException {

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(mapUser(rs));
        }
    }
}
