package org.yomirein.sochatserver;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.XSlf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.calls.CallHandler;
import org.yomirein.sochatserver.calls.CallService;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.common.managers.ChallengeManager;
import org.yomirein.sochatserver.friendship.FriendsHandler;
import org.yomirein.sochatserver.media.MediaHandler;
import org.yomirein.sochatserver.media.MediaRepository;
import org.yomirein.sochatserver.media.MediaService;
import org.yomirein.sochatserver.messages.MessageHandler;
import org.yomirein.sochatserver.messages.MessageRepository;
import org.yomirein.sochatserver.messages.MessageService;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.netty.HttpServer;
import org.yomirein.sochatserver.chats.ChatRepository;
import org.yomirein.sochatserver.friendship.FriendshipRepository;
import org.yomirein.sochatserver.common.repos.TrustKeysRepository;
import org.yomirein.sochatserver.users.UserRepository;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.friendship.FriendshipService;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.users.UsersHandler;

public class SoChat {
    public void run() throws Exception {

        // BIG INITIALIZATION
        //
        // I separated every type
        //
        // And running server, it works on HTTP and WebSocket(Class named HttpServer, because WebSocket works on HTTP anyway)


        Logger LOGGER = LoggerFactory.getLogger(this.getClass());

        LOGGER.info("Starting SoChat server...");

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        // Managers
        ChallengeManager challengeManager = new ChallengeManager();
        SessionManager sessionManager = new SessionManager();

        // Repositories initialization
        UserRepository userRepository = new UserRepository();
        TrustKeysRepository trustKeysRepository = new TrustKeysRepository();
        ChatRepository chatRepository = new ChatRepository();
        FriendshipRepository friendshipRepository = new FriendshipRepository();
        MessageRepository messageRepository = new MessageRepository();
        MediaRepository mediaRepository = new MediaRepository();

        // Services initialization
        AuthService authService = new AuthService(challengeManager, userRepository);
        FriendshipService friendshipService = new FriendshipService(friendshipRepository, userRepository, trustKeysRepository);
        UserService userService = new UserService(userRepository);
        ChatService chatService = new ChatService(userService, chatRepository);
        MediaService mediaService = new MediaService(mediaRepository, chatService, userService);
        MessageService messageService = new MessageService(messageRepository, mediaService);
        CallService callService = new CallService(sessionManager);

        // Handlers initialization
        AuthHandler authHandler = new AuthHandler(userRepository,sessionManager);
        FriendsHandler friendsHandler = new FriendsHandler(sessionManager, userRepository, friendshipRepository, friendshipService, userService);
        UsersHandler userHandler = new UsersHandler(sessionManager, userRepository, trustKeysRepository, userService);
        ChatHandler chatHandler = new ChatHandler(chatService, userService, messageService, callService, sessionManager);
        MessageHandler messageHandler = new MessageHandler(messageService, chatService, userService, mediaService, sessionManager);
        MediaHandler mediaHandler = new MediaHandler(mediaService);
        CallHandler callHandler = new CallHandler(callService, friendshipService, userService, chatService, sessionManager);

        // Server initialization
        HttpServer httpServer = new HttpServer(8081, authService, callService, sessionManager, authHandler, friendsHandler,
                userHandler, chatHandler, messageHandler, mediaHandler, callHandler);

        // Run everything
        httpServer.run();

    }
}