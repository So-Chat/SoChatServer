package org.yomirein.sochatserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.calls.CallHandler;
import org.yomirein.sochatserver.calls.CallService;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.common.managers.ChallengeManager;
import org.yomirein.sochatserver.friendship.FriendsHandler;
import org.yomirein.sochatserver.friendship.FriendshipService;
import org.yomirein.sochatserver.media.MediaHandler;
import org.yomirein.sochatserver.media.MediaService;
import org.yomirein.sochatserver.messages.MessageHandler;
import org.yomirein.sochatserver.messages.MessageService;
import org.yomirein.sochatserver.search.SearchHandler;
import org.yomirein.sochatserver.search.SearchService;
import org.yomirein.sochatserver.netty.HttpServer;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.persistance.api.repositories.*;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.users.UsersHandler;

import org.yomirein.sochatserver.persistance.api.Database;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

public class SoChat {
    public void run(Database database) throws Exception {

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
        UserRepository userRepository = database.getUserRepository();
        TrustKeysRepository trustKeysRepository = database.getTrustKeysRepository();
        ChatRepository chatRepository = database.getChatRepository();
        FriendshipRepository friendshipRepository = database.getFriendshipRepository();
        MessageRepository messageRepository = database.getMessageRepository();
        MediaRepository mediaRepository = database.getMediaRepository();

        // Services initialization
        AuthService authService = new AuthService(challengeManager, userRepository);
        FriendshipService friendshipService = new FriendshipService(friendshipRepository, userRepository, trustKeysRepository);
        UserService userService = new UserService(userRepository);
        ChatService chatService = new ChatService(userService, chatRepository);
        MediaService mediaService = new MediaService(mediaRepository, userService);
        MessageService messageService = new MessageService(messageRepository, mediaService);
        CallService callService = new CallService(sessionManager);
        SearchService searchService = new SearchService(userRepository);

        // Handlers initialization
        AuthHandler authHandler = new AuthHandler(userService,sessionManager);
        FriendsHandler friendsHandler = new FriendsHandler(sessionManager, friendshipService, userService);
        UsersHandler userHandler = new UsersHandler(sessionManager, trustKeysRepository, userService);
        ChatHandler chatHandler = new ChatHandler(chatService, userService, messageService, callService, sessionManager);
        MessageHandler messageHandler = new MessageHandler(messageService, chatService, userService, mediaService, sessionManager);
        MediaHandler mediaHandler = new MediaHandler(mediaService, chatService);
        CallHandler callHandler = new CallHandler(callService, friendshipService, userService, chatService, sessionManager);
        SearchHandler searchHandler = new SearchHandler(searchService);

        // Server initialization
        HttpServer httpServer = new HttpServer(8081, authService, callService, sessionManager, authHandler, friendsHandler,
                userHandler, chatHandler, messageHandler, mediaHandler, callHandler, searchHandler);

        // Run everything
        httpServer.run();

    }
}
