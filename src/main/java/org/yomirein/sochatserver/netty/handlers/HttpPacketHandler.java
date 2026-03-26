package org.yomirein.sochatserver.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.List;
import java.util.Map;

public class HttpPacketHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private final AuthService authService;

    public HttpPacketHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        String uri = fullHttpRequest.uri();

        if ("/".equals(fullHttpRequest.uri())) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.content().writeBytes("Hello HTTP!".getBytes());
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            channelHandlerContext.writeAndFlush(response);
        } else {
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        }


        if (fullHttpRequest.method().equals(HttpMethod.GET)) {
            if (!uri.contains("/auth/login?username=")){
                return;
            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = queryStringDecoder.parameters();

            MessagePacket challengeResponse = authService.createChallenge(String.valueOf(params.get("username").getFirst()));

            HttpResponseStatus challengeStatus = challengeResponse.payload.get("success").toString().equals("true")
                    ? HttpResponseStatus.OK
                    : HttpResponseStatus.NOT_ACCEPTABLE;

            sendJson(channelHandlerContext, challengeStatus, JsonConfig.MAPPER.writeValueAsString(challengeResponse));
        }


        else if (fullHttpRequest.method().equals(HttpMethod.POST) ) {
            String body = fullHttpRequest.content()
                    .toString(CharsetUtil.UTF_8);

            Map<String, Object> map = JsonConfig.MAPPER.readValue(body, Map.class);
            Map<String, Object> payload = (Map<String, Object>) map.get("payload");

            switch (uri){
                case ("/auth/register"):
                    MessagePacket registerResponse  = authService.register(String.valueOf(payload.get("username")),
                            String.valueOf(payload.get("ed25519PublicKey")), String.valueOf(payload.get("x25519PublicKey")));

                    HttpResponseStatus registerStatus = registerResponse.payload.get("success").toString().equals("true")
                            ? HttpResponseStatus.OK
                            : HttpResponseStatus.NOT_ACCEPTABLE;

                    sendJson(channelHandlerContext, registerStatus, JsonConfig.MAPPER.writeValueAsString(registerResponse));


                    break;
                case ("/auth/verify"):
                    MessagePacket verifyResponse = authService.login(String.valueOf(payload.get("username")),
                            String.valueOf(payload.get("signature")),
                            String.valueOf(payload.get("challenge")));

                    HttpResponseStatus verifyStatus = verifyResponse.payload.get("success").toString().equals("true")
                            ? HttpResponseStatus.OK
                            : HttpResponseStatus.NOT_ACCEPTABLE;

                    sendJson(channelHandlerContext, verifyStatus, JsonConfig.MAPPER.writeValueAsString(verifyResponse));
                    break;
            }


        }

    }

    private void sendJson(ChannelHandlerContext ctx, HttpResponseStatus httpResponseStatus, String json) {

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
}
