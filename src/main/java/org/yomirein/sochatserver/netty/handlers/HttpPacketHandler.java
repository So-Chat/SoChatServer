package org.yomirein.sochatserver.netty.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.utils.JsonConfig;
import org.yomirein.sochatserver.utils.MessageSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

// HttpPacketHandler using for register, validate user and then authenticate user
@AllArgsConstructor
public class HttpPacketHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final AuthService authService;

    private final Path root = Paths.get("uploads").toAbsolutePath().normalize();

    // Read requests
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        String uri = fullHttpRequest.uri();

        // Basic answer if someone got into ./
        if ("/".equals(fullHttpRequest.uri())) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.content().writeBytes("SoChat!".getBytes());
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            channelHandlerContext.writeAndFlush(response);
        } else {
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        }


        // GET Requests
        if (fullHttpRequest.method().equals(HttpMethod.GET)) {
            // Sends challenge to complete in 5 minutes after sending from server
            if (!uri.contains("/auth/login?username=")){
                return;
            }
            // Get user data
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = queryStringDecoder.parameters();

            // AuthService creates challenge
            MessagePacket challengeResponse = authService.createChallenge(String.valueOf(params.get("username").getFirst()));

            // Send answer
            configureResponseAndSend(channelHandlerContext, challengeResponse);
        }

        // POST Requests
        else if (fullHttpRequest.method().equals(HttpMethod.POST) ) {
            String body = fullHttpRequest.content()
                    .toString(CharsetUtil.UTF_8);

            // Getting json from request
            Map<String, Object> map = JsonConfig.MAPPER.readValue(body, Map.class);
            Map<String, Object> payload = (Map<String, Object>) map.get("payload");

            switch (uri){
                // AuthService works like handler and service because of its easy work

                // If it's registration we register user with AuthService.register()
                case ("/auth/register"):
                    MessagePacket registerResponse  = authService.register(String.valueOf(payload.get("username")),
                            String.valueOf(payload.get("ed25519PublicKey")), String.valueOf(payload.get("x25519PublicKey")));
                    // Send answer
                    configureResponseAndSend(channelHandlerContext, registerResponse);
                    break;

                // And verifying user with checking for challenge competion
                case ("/auth/verify"):
                    MessagePacket verifyResponse = authService.login(String.valueOf(payload.get("username")),
                            String.valueOf(payload.get("signature")),
                            String.valueOf(payload.get("challenge")));
                    // Send answer
                    configureResponseAndSend(channelHandlerContext, verifyResponse);
                    break;
            }


        }

    }


    // Configure HttpResponseStatus using "success" from messagePacket if it does not provided
    private void configureResponseAndSend(ChannelHandlerContext ctx, MessagePacket messagePacket) throws JsonProcessingException {
        HttpResponseStatus httpResponseStatus = messagePacket.payload.get("success").toString().equals("true")
                ? OK
                : BAD_REQUEST;
        sendJson(ctx, httpResponseStatus, messagePacket);
    }

    // Send json, I also used the same method in https://github.com/yomirein/AuthenticationServer :D
    private void sendJson(ChannelHandlerContext ctx, HttpResponseStatus httpResponseStatus, MessagePacket messagePacket) throws JsonProcessingException {
        String json = JsonConfig.MAPPER.writeValueAsString(messagePacket);

        // Making our response understandable for server
        ByteBuf content = Unpooled.copiedBuffer(
                json, CharsetUtil.UTF_8
        );

        //Then we make a response that we can send, making response status and setting our content for response
        FullHttpResponse response =
                new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        httpResponseStatus,
                        content
                );


        // Setting basic headers
        response.headers().set(
                HttpHeaderNames.CONTENT_TYPE,
                "application/json; charset=UTF-8"
        );
        response.headers().setInt(
                HttpHeaderNames.CONTENT_LENGTH,
                content.readableBytes()
        );

        // Sending to user!!
        ctx.writeAndFlush(response);
    }

    public void getMedia(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws IOException {
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            sendJson(ctx, BAD_REQUEST, new MessagePacket.Builder()
                    .put("server_message", "Bad request")
                    .put("success", false)
                    .build());
            return;
        }

        if (fullHttpRequest.method() != HttpMethod.GET) {
            sendJson(ctx, METHOD_NOT_ALLOWED, new MessagePacket.Builder()
                    .put("server_message", "Only GET allowed here")
                    .put("success", false)
                    .build());
            return;
        }

        String uri = fullHttpRequest.uri();

        if (!uri.startsWith("/files/")) {
            sendJson(ctx, NOT_FOUND, new MessagePacket.Builder()
                    .put("server_message", "Not found")
                    .put("success", false)
                    .build());
            return;
        }

        String relative = uri.substring("/files/".length());

        Path requested = root.resolve(relative).normalize();

        if (!requested.startsWith(root)) {
            sendJson(ctx, FORBIDDEN, new MessagePacket.Builder()
                    .put("server_message", "Access denied")
                    .put("success", false)
                    .build());
            return;
        }

        File file = requested.toFile();
        if (!file.exists() || !file.isFile()) {
            sendJson(ctx, NOT_FOUND, new MessagePacket.Builder()
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
}
