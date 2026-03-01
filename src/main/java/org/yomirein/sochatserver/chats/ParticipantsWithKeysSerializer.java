package org.yomirein.sochatserver.chats;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.utils.JsonConfig;
import org.yomirein.sochatserver.utils.KeyParser;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticipantsWithKeysSerializer extends JsonSerializer<Map<User, String>> {

    @Override
    public void serialize(Map<User, String> participantsWithKeys, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        List<ParticipantDTO> participantDTOs = new ArrayList<>();

        for (var entry : participantsWithKeys.entrySet()){
            participantDTOs.add(new ParticipantDTO(entry.getKey().getId(), entry.getKey().getUsername()));
        }

        gen.writeObject(participantDTOs);
    }
}