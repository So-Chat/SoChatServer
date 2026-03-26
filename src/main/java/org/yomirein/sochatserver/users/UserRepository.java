package org.yomirein.sochatserver.users;

import org.yomirein.sochatserver.Database;
import org.yomirein.sochatserver.utils.KeyParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

import static org.yomirein.sochatserver.utils.JsonConfig.mapUser;

public class UserRepository {

    public User saveUser(User user) {
        String sql = "INSERT INTO users(nickname, username, description, ed25519_public_key, x25519_public_key) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection dbConnection = Database.getConnection();
             PreparedStatement ps = dbConnection.prepareStatement(sql)) {

            ps.setString(1, user.getNickname());
            ps.setString(2, user.getUsername());
            ps.setString(3, Base64.getEncoder().encodeToString(user.getEd25519PublicKey().getEncoded()));
            ps.setString(4, Base64.getEncoder().encodeToString(user.getX25519PublicKey().getEncoded()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Optional<User> findByName(String username) {
        String sql = "SELECT id, nickname, username, description, ed25519_public_key, x25519_public_key FROM users WHERE username = ?";
        try (Connection dbConnection = Database.getConnection();
            PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapUser(rs));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT id, nickname, username, description, ed25519_public_key, x25519_public_key FROM users WHERE id = ?";
        try (Connection dbConnection = Database.getConnection();
             PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(mapUser(rs));
            }
        }
        catch (SQLException ex) {

        }
        return Optional.empty();
    }
    
    public boolean updateUser(Long id, String username, String nickname, String description) {
        String sql = "UPDATE users SET username = COALESCE(?, username), nickname = COALESCE(?, nickname), description = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, nickname);
            ps.setString(3, description);
            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
