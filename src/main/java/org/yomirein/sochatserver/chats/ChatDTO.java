package org.yomirein.sochatserver.chats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yomirein.sochatserver.users.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private long id;
    private String chatKey;
    private List<ParticipantDTO> participants = new ArrayList<>();
}
