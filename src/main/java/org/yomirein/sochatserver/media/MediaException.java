package org.yomirein.sochatserver.media;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

@Getter
class MediaException extends Exception {
    private final HttpResponseStatus status;
    public MediaException(HttpResponseStatus status, String message) {
        super(message);
        this.status = status;
    }
}
