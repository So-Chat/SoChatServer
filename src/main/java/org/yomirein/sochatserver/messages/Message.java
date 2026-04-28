package org.yomirein.sochatserver.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yomirein.sochatserver.media.Media;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<Media> mediaFiles;

    private String content;
    private LocalDateTime timestamp;
}