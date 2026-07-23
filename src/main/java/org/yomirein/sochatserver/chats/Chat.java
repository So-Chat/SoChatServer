package org.yomirein.sochatserver.chats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.yomirein.sochatserver.calls.CallState;
import org.yomirein.sochatserver.messages.Message;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private long id;
    private String title;
    private ChatType chatType;
    private List<SenderKey> senderKeys;
    private List<Participant> participants;

    private Message lastMessage;
    private SenderKey lastSenderKey;
    private Integer unreadMessagesCount;

    private CallState callState = CallState.IDLE;
}
