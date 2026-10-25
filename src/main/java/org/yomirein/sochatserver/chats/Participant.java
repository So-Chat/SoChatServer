package org.yomirein.sochatserver.chats;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Participant {
    private Long chatId;
    private long userId;
    private ChatRole chatRole;

    private long lastMessageId = 0;

    // Constructor without lastMessageId because it can be without it
    public Participant(Long chatId, long userId, ChatRole chatRole) {
        this.chatId = chatId;
        this.userId = userId;
        this.chatRole = chatRole;
    }
}
