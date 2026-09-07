package org.yomirein.sochatserver.utils;

import com.fasterxml.jackson.databind.*;
import org.yomirein.sochatserver.users.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonConfig {
    // Init static ObjectMapper so we can use it anywhere
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Easier getting Text so we don't get big checks everytime in code
    public static String getTextOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
    // Same for Longs
    public static Long getLongOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asLong() : null;
    }
    public static Integer getIntOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asInt() : null;
    }

}
