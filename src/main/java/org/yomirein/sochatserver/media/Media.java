package org.yomirein.sochatserver.media;

import lombok.*;

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
}
