package org.yomirein.sochatserver.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.BackgroundCleanerWorker;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.calls.CallHandler;
import org.yomirein.sochatserver.calls.CallService;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.friendship.FriendsHandler;
import org.yomirein.sochatserver.media.MediaHandler;
import org.yomirein.sochatserver.messages.MessageHandler;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public record SoChatServer(
    int port,
    CallService callService,
    SessionManager sessionManager,
    AuthHandler authHandler,
    FriendsHandler friendsHandler,
    UsersHandler usersHandler,
    ChatHandler chatHandler,
    MessageHandler messageHandler,
    MediaHandler mediaHandler,
    CallHandler callHandler,
    SearchHandler searchHandler,
    BackgroundCleanerWorker backgroundCleanerWorker
) {
    private static final int MAX_FRAME_SIZE = 65536;
    private static final Logger logger = LoggerFactory.getLogger(SoChatServer.class);

    public void run() throws Exception {
        logger.info("Starting Server");

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

                            p.addLast(new ProtocolConfigurator(MAX_FRAME_SIZE, callService, sessionManager, authHandler, friendsHandler, usersHandler, chatHandler, messageHandler, mediaHandler, callHandler, searchHandler));
                        }
                    });

            // Starting server
            ChannelFuture future = b.bind(port).sync();
            future.channel().closeFuture().sync();

            backgroundCleanerWorker.run();
        }
        finally {
            workerGroup.shutdownGracefully();
        }
    }
}
