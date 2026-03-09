package org.yomirein.sochatserver.chats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    private Long chatId;
    private long userId;
    private ChatRole chatRole;
}
