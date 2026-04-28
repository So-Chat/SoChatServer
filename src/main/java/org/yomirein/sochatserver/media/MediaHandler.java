package org.yomirein.sochatserver.media;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.stream.ChunkedFile;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.utils.MessageSender;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.yomirein.sochatserver.utils.MessageSender.sendHttp;

@RequiredArgsConstructor
public class MediaHandler {

    private final MediaService mediaService;


    public void getMedia(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            MessageSender.sendHttp(ctx, BAD_REQUEST, "Bad request");
            return;
        }

        if (fullHttpRequest.method() != HttpMethod.GET) {
            MessageSender.sendHttp(ctx, METHOD_NOT_ALLOWED, "Only GET allowed");
            return;
        }

        try {
            Media media = mediaService.getMediaFile(fullHttpRequest.uri());

            RandomAccessFile raf = new RandomAccessFile(media.getFile(), "r");
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

            String contentType = URLConnection.guessContentTypeFromName(media.getFile().getName());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream");
            response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

            response.headers().set(
                    HttpHeaderNames.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + media.getFileName() + "\""
            );

            ctx.write(response);
            ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf)))
                    .addListener(ChannelFutureListener.CLOSE);

        } catch (MediaException e) {
            sendHttp(ctx, e.getStatus(), e.getMessage());
        } catch (IOException e) {
            sendHttp(ctx, INTERNAL_SERVER_ERROR, "IO Error");
            throw new RuntimeException(e);
        }
    }

    public void uploadMedia(ChannelHandlerContext ctx, FullHttpRequest request) {
        DefaultHttpDataFactory factory = new DefaultHttpDataFactory(true);
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);

        try {
            String token = null;

            String authHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }

            FileUpload file = null;

            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();

                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;

                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    file = (FileUpload) data;
                }
            }
            if (file != null) {
                try {
                    String mediaId = mediaService.saveUploadedFile(token, file);
                    sendHttp(ctx, OK, mediaId);
                } catch (MediaException e) {
                    sendHttp(ctx, e.getStatus(), e.getMessage());
                } catch (IOException e) {
                    sendHttp(ctx, INTERNAL_SERVER_ERROR, "Contact administrator about this error");
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendHttp(ctx, INTERNAL_SERVER_ERROR, "Upload failed");
        } finally {
            decoder.destroy();
        }
    }


}
