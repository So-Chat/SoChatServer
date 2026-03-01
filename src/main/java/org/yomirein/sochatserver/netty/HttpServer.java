package org.yomirein.sochatserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.messages.MessageRepository;
import org.yomirein.sochatserver.messages.MessageService;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.netty.codec.PacketDecoder;
import org.yomirein.sochatserver.netty.codec.PacketEncoder;
import org.yomirein.sochatserver.netty.handlers.WsPacketHandler;
import org.yomirein.sochatserver.common.managers.ChallengeManager;
import org.yomirein.sochatserver.netty.handlers.HttpPacketHandler;
import org.yomirein.sochatserver.friendship.FriendshipRepository;
import org.yomirein.sochatserver.common.repos.TrustKeysRepository;
import org.yomirein.sochatserver.users.UserRepository;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.friendship.FriendshipService;
import org.yomirein.sochatserver.users.UserService;


public class HttpServer {

    private final int port;

    private final AuthService authService;
    private final FriendshipService friendshipService;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageService messageService;

    private final ChallengeManager challengeManager;
    private final SessionManager sessionManager;

    private final TrustKeysRepository trustKeysRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final MessageRepository messageRepository;

    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);


    public HttpServer(AuthService authService, FriendshipService friendshipService, UserService userService, ChatService chatService, MessageService messageService,
                      ChallengeManager challengeManager, SessionManager sessionManager,
                      TrustKeysRepository trustKeysRepository, UserRepository userRepository, FriendshipRepository friendshipRepository, MessageRepository messageRepository,
                      int port) {
        this.authService = authService;
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.chatService = chatService;
        this.messageService = messageService;

        this.challengeManager = challengeManager;
        this.sessionManager = sessionManager;

        this.trustKeysRepository = trustKeysRepository ;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;

        this.port = port;
    }


    public void run() throws Exception {
        logger.info("Starting Http Server");

        // EventLoopGroups
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin() //
                // Allows all origins
                .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
                .allowCredentials() // If you need to support cookies/credentials
                .allowedRequestHeaders("X-Requested-With", "Content-Type", "Content-Length") // Allowed client headers
                .exposeHeaders("Content-Disposition") // Headers exposed to the client browser
                .build();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();


                            p.addLast(new HttpServerCodec());
                            p.addLast(new HttpObjectAggregator(65536));

                            p.addLast(new WebSocketServerProtocolHandler("/ws", null, true));

                            p.addLast(new CorsHandler(corsConfig));
                            p.addLast(new HttpPacketHandler(authService));

                            p.addLast(new PacketDecoder());
                            p.addLast(new WsPacketHandler(sessionManager,
                                    userRepository, friendshipRepository, trustKeysRepository, messageRepository,
                                    friendshipService, userService, chatService, messageService));
                            p.addLast(new PacketEncoder());
                        }
                    });

            // Starting server
            Channel channel = b.bind(port).sync().channel();
            channel.closeFuture().sync();
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
