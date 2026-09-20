package org.yomirein.sochatserver.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.calls.CallHandler;
import org.yomirein.sochatserver.calls.CallService;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.friendship.FriendsHandler;
import org.yomirein.sochatserver.media.MediaHandler;
import org.yomirein.sochatserver.messages.MessageHandler;
import org.yomirein.sochatserver.netty.codec.TcpPacketDecoder;
import org.yomirein.sochatserver.netty.codec.TcpPacketEncoder;
import org.yomirein.sochatserver.netty.handlers.HeartbeatHandler;
import org.yomirein.sochatserver.netty.handlers.PacketHandler;
import org.yomirein.sochatserver.search.SearchHandler;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.UsersHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TcpServer {

    // Imports from SoChat.java
    private final int port;

    private final AuthService authService;
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

    // Adding logger
    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public void run() throws Exception {
        logger.info("Starting Tcp Server");

        // EventLoopGroups
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))

                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

                .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new LoggingHandler(LogLevel.DEBUG));

                            p.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));

                            // Heartbeat for low-level ping pongs
                            p.addLast(new IdleStateHandler(0, 20, 0));
                            p.addLast(new HeartbeatHandler(sessionManager));


                            // WsPacketHandler init, with decoders and encoders
                            p.addLast(new TcpPacketDecoder());
                            p.addLast(new PacketHandler(sessionManager, authHandler,
                                    friendsHandler, usersHandler, chatHandler, messageHandler, callHandler, searchHandler, callService));
                            p.addLast(new TcpPacketEncoder());
                        }
                    });

            // Starting server
            ChannelFuture future = b.bind(port).sync();
            future.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
        }
    }
}
