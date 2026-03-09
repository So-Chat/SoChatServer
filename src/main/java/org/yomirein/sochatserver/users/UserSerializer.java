package org.yomirein.sochatserver.users;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.io.IOException;

public class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeFieldName(JsonConfig.MAPPER.writeValueAsString(value));
    }
}