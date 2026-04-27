package org.yomirein.sochatserver.media;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.stream.ChunkedFile;
import org.yomirein.sochatserver.common.models.MessagePacket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.yomirein.sochatserver.utils.MessageSender.sendHttpJson;

public class MediaService {

    private final Path root = Paths.get("uploads").toAbsolutePath().normalize();

    public void getMedia(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws IOException {
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            sendHttpJson(ctx, BAD_REQUEST, new MessagePacket.Builder()
                    .put("server_message", "Bad request")
                    .put("success", false)
                    .build());
            return;
        }

        if (fullHttpRequest.method() != HttpMethod.GET) {
            sendHttpJson(ctx, METHOD_NOT_ALLOWED, new MessagePacket.Builder()
                    .put("server_message", "Only GET allowed here")
                    .put("success", false)
                    .build());
            return;
        }

        String uri = fullHttpRequest.uri();

        if (!uri.startsWith("/media/")) {
            sendHttpJson(ctx, NOT_FOUND, new MessagePacket.Builder()
                    .put("server_message", "Not found")
                    .put("success", false)
                    .build());
            return;
        }

        String relative = uri.substring("/media/".length());

        Path requested = root.resolve(relative).normalize();

        if (!requested.startsWith(root)) {
            sendHttpJson(ctx, FORBIDDEN, new MessagePacket.Builder()
                    .put("server_message", "Access denied")
                    .put("success", false)
                    .build());
            return;
        }

        File file = requested.toFile();
        if (!file.exists() || !file.isFile()) {
            sendHttpJson(ctx, NOT_FOUND, new MessagePacket.Builder()
                    .put("server_message", "File not found")
                    .put("success", false)
                    .build());
            return;
        }

        RandomAccessFile raf = new RandomAccessFile(file, "r");

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        ctx.write(response);
        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf)))
                .addListener(ChannelFutureListener.CLOSE);
    }


    // I'm really trying to figure out how it works...
    public void uploadMedia(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        //String token = fullHttpRequest.headers().get(HttpHeaderNames.AUTHORIZATION).substring(7);
        String contentType = fullHttpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
        DefaultHttpDataFactory factory = new DefaultHttpDataFactory(true);
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, fullHttpRequest);

        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();

                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    System.out.println("Поле: " + attribute.getName() + " = " + attribute.getValue());

                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    System.out.println("Файл: " + fileUpload.getFilename());
                    fileUpload.renameTo(new File(root.resolve(fileUpload.getFilename()).toAbsolutePath().toString()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            decoder.destroy();
        }
    }

}
