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
import org.yomirein.sochatserver.media.MediaService;
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
import static org.yomirein.sochatserver.utils.MessageSender.sendHttpJson;

// HttpPacketHandler using for register, validate user and then authenticate user
@AllArgsConstructor
public class HttpPacketHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final AuthService authService;
    private final MediaService mediaService;


    // Read requests
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        String uri = fullHttpRequest.uri();
        System.out.println(uri);
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

            if (uri.contains("/auth/login?username=")){
                // Get user data
                // Sends challenge to complete in 5 minutes after sending from server
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
                Map<String, List<String>> params = queryStringDecoder.parameters();

                // AuthService creates challenge
                MessagePacket challengeResponse = authService.createChallenge(String.valueOf(params.get("username").getFirst()));

                // Send answer
                configureResponseAndSend(channelHandlerContext, challengeResponse);
            }
            else if (uri.contains("/media")){
                mediaService.getMedia(channelHandlerContext, fullHttpRequest);
            }

        }

        // POST Requests
        else if (fullHttpRequest.method().equals(HttpMethod.POST) ) {
            String body = fullHttpRequest.content()
                    .toString(CharsetUtil.UTF_8);

            // Getting json from request
            if (uri.startsWith("/auth/")) {
                Map<String, Object> map = JsonConfig.MAPPER.readValue(body, Map.class);
                Map<String, Object> payload = (Map<String, Object>) map.get("payload");
                switch (uri) {
                    // AuthService works like handler and service because of its easy work

                    // If it's registration we register user with AuthService.register()
                    case ("/auth/register"):
                        MessagePacket registerResponse = authService.register(String.valueOf(payload.get("username")),
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

            else if (uri.startsWith("/media")) {
                mediaService.uploadMedia(channelHandlerContext, fullHttpRequest);
            }


        }

    }


    // Configure HttpResponseStatus using "success" from messagePacket if it does not provided
    private void configureResponseAndSend(ChannelHandlerContext ctx, MessagePacket messagePacket) throws JsonProcessingException {
        HttpResponseStatus httpResponseStatus = messagePacket.payload.get("success").toString().equals("true")
                ? OK
                : BAD_REQUEST;
        sendHttpJson(ctx, httpResponseStatus, messagePacket);
    }
}
