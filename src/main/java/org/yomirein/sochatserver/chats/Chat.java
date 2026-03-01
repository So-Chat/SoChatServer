package org.yomirein.sochatserver.chats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.utils.PublicKeySerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private long id;
    private Map<User, String> participantsWithKeys = new HashMap<>();

    public ChatDTO toDTO(long userId) {
        ChatDTO dto = new ChatDTO();
        List<ParticipantDTO> participantsDTO = new ArrayList<>();
        for (var entry : participantsWithKeys.entrySet()) {
            participantsDTO.add(new ParticipantDTO(entry.getKey().getId(), entry.getKey().getUsername()));
            if (entry.getKey().getId() == userId) {
                dto.setChatKey(entry.getValue());
            }
        }
        dto.setParticipants(participantsDTO);
        dto.setId(id);

        return dto;
    }
}