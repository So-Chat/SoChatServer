package org.yomirein.sochatserver.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.yomirein.sochatserver.users.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonConfig {
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static final ObjectWriter API_WRITER =
            MAPPER.writer();

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

}
