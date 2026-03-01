package org.yomirein.sochatserver.users;

import org.yomirein.sochatserver.Database;
import org.yomirein.sochatserver.utils.KeyParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

public class UserRepository {

    public User saveUser(User user) {
        String sql = "INSERT INTO users(username, ed25519_public_key, x25519_public_key) VALUES (?, ?, ?) RETURNING id";
        try (Connection dbConnection = Database.getConnection();
             PreparedStatement ps = dbConnection.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, Base64.getEncoder().encodeToString(user.getEd25519PublicKey().getEncoded()));
            ps.setString(3, Base64.getEncoder().encodeToString(user.getX25519PublicKey().getEncoded()));

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
        String sql = "SELECT id, username, ed25519_public_key, x25519_public_key FROM users WHERE username = ?";
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
        String sql = "SELECT id, username, ed25519_public_key, x25519_public_key FROM users WHERE id = ?";
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

    private User mapUser(ResultSet rs) throws SQLException {
        User u = null;
        try {

            u = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    KeyParser.stringToPublicKeyED25519(rs.getString("ed25519_public_key")),
                    KeyParser.stringToPublicKeyX25519(rs.getString("x25519_public_key"))
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }

}
