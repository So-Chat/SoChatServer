package org.yomirein.sochatserver.media;

import lombok.*;

import java.io.File;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Media {
    private String mediaId;
    private Long messageId;

    private long senderId;

    private String mimeType;

    private String fileName;
    private long fileSize;

    private Integer width;
    private Integer height;
    private Integer length;

    private String nonce;

    private File file;

    public Media(String mediaId, Long messageId,
                 long senderId, String mimeType,
                 String fileName, long fileSize,
                 Integer width, Integer height, Integer length, String nonce){

        this.mediaId = mediaId;
        this.messageId = messageId;
        this.senderId = senderId;

        this.mimeType = mimeType;
        this.fileName = fileName;
        this.fileSize = fileSize;

        this.width = width;
        this.height = height;
        this.length = length;

        this.nonce = nonce;

    }
}
