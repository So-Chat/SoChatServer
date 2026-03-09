package org.yomirein.sochatserver.chats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SenderKey {
    private Long chatId;
    private long userId;
    private int keyVersion;
    private String chatKey;
}
