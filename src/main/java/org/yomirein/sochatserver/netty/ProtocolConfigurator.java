package org.yomirein.sochatserver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.AllArgsConstructor;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.calls.CallHandler;
import org.yomirein.sochatserver.calls.CallService;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.friendship.FriendsHandler;
import org.yomirein.sochatserver.media.MediaHandler;
import org.yomirein.sochatserver.messages.MessageHandler;
import org.yomirein.sochatserver.netty.codec.TcpPacketDecoder;
import org.yomirein.sochatserver.netty.codec.TcpPacketEncoder;
import org.yomirein.sochatserver.netty.handlers.HeartbeatHandler;
import org.yomirein.sochatserver.netty.handlers.HttpPacketHandler;
import org.yomirein.sochatserver.netty.handlers.PacketHandler;
import org.yomirein.sochatserver.search.SearchHandler;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.UsersHandler;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

@AllArgsConstructor
public class ProtocolConfigurator extends ChannelInboundHandlerAdapter {

    private final int MAX_FRAME_SIZE;

    private final CallService callService;

    private final SessionManager sessionManager;

    private final AuthHandler authHandler;
    private final FriendsHandler friendsHandler;
    private final UsersHandler usersHandler;
    private final ChatHandler chatHandler;
    private final MessageHandler messageHandler;
    private final MediaHandler mediaHandler;
    private final CallHandler callHandler;
    private final SearchHandler searchHandler;

    private final CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
        // Allows all origins
        .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
        .allowCredentials() // To support cookies/credentials in the future
        .allowedRequestHeaders("X-Requested-With", "Content-Type", "Content-Length") // Allowed client headers
        .exposeHeaders("Content-Disposition") // Headers exposed to the client browser
        .build();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;

        if (in.readableBytes() < 4) {
            ctx.fireChannelRead(msg);
            return;
        }

        if (isTcpProtocol(in)) {
            switchToTcp(ctx);
        } else {
            switchToHttp(ctx);
        }

        ctx.pipeline().remove(this);
        ctx.pipeline().firstContext().fireChannelRead(msg);
    }

    private boolean isTcpProtocol(ByteBuf in) {
        int length = in.getInt(in.readerIndex());

        return length >= 0 && length <= MAX_FRAME_SIZE;
    }

    private void switchToHttp(ChannelHandlerContext channelHandlerContext) {
        ChannelPipeline p = channelHandlerContext.pipeline();

        p.addLast("http-logging", new LoggingHandler(LogLevel.TRACE));

        // HTTP Server
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(655369999));

        // WebSocket server protocol init
        p.addLast(new WebSocketServerProtocolHandler("/ws", null, true));

        // Heartbeat for low-level ping pongs
        p.addLast(new IdleStateHandler(0, 20, 0));
        p.addLast(new HeartbeatHandler(sessionManager));

        // Cors
        p.addLast(new CorsHandler(corsConfig));
        // HttpPacketHandler init
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new HttpPacketHandler(mediaHandler));
    }

    private void switchToTcp(ChannelHandlerContext channelHandlerContext) {
            ChannelPipeline p = channelHandlerContext.pipeline();

            p.addLast("tcp-frame-decoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_SIZE, 0, 4, 0, 4));
            p.addLast(new LoggingHandler(LogLevel.DEBUG));

            // Heartbeat for low-level ping pongs
            p.addLast(new IdleStateHandler(0, 20, 0));
            p.addLast(new HeartbeatHandler(sessionManager));

            // WsPacketHandler init, with decoders and encoders
            p.addLast(new TcpPacketDecoder());
            p.addLast(new PacketHandler(sessionManager, authHandler,
                    friendsHandler, usersHandler, chatHandler, messageHandler, callHandler, searchHandler, callService));
            p.addLast(new TcpPacketEncoder());
    }
}
