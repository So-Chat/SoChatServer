package org.yomirein.sochatserver.chats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yomirein.sochatserver.messages.Message;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserSerializer;
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
    private String title;
    private ChatType chatType;
    private List<SenderKey> senderKeys;
    private List<Participant> participants;

    private Message lastMessage;
    private SenderKey lastSenderKey;
    private Integer unreadMessagesCount;
}