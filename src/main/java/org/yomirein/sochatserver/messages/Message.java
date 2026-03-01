package org.yomirein.sochatserver.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private long id;
    private long chatId;

    private long senderId;
    private Long replyMessageId;

    private int keyVersion;

    private String content;
    private LocalDateTime timestamp;
}