package org.yomirein.sochatserver.netty.handlers;

import org.yomirein.sochatserver.media.MediaHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.AllArgsConstructor;

// HttpPacketHandler using for register, validate user and then authenticate user
@AllArgsConstructor
public class HttpPacketHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final MediaHandler mediaHandler;


    // Read requests
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        String uri = fullHttpRequest.uri();
        // Basic answer if someone got into ./
        if ("/".equals(fullHttpRequest.uri())) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.content().writeBytes("SoChat Http!".getBytes());
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            channelHandlerContext.writeAndFlush(response);
        } else {
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        }

        if (fullHttpRequest.method().equals(HttpMethod.GET)) {
            if (uri.contains("/media")){
                mediaHandler.getMedia(channelHandlerContext, fullHttpRequest);
            }
        }
        if (fullHttpRequest.method().equals(HttpMethod.POST) ) {
            if (uri.startsWith("/media")) {
                mediaHandler.uploadMedia(channelHandlerContext, fullHttpRequest);
            }
        } else if (fullHttpRequest.method().equals(HttpMethod.DELETE)) {
            if (uri.contains("/media")){
                mediaHandler.deleteMedia(channelHandlerContext, fullHttpRequest);
            }
        }

    }
}
