package org.yomirein.sochatserver.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.security.PublicKey;

public class PublicKeySerializer extends JsonSerializer<PublicKey> {

    @Override
    public void serialize(PublicKey publicKey, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (publicKey == null) {
            gen.writeNull();
            return;
        }
        String base64 = KeyParser.convertPublicKeyToString(publicKey);
        gen.writeString(base64);
    }
}