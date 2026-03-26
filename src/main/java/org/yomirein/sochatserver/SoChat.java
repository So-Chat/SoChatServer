package org.yomirein.sochatserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.yomirein.sochatserver.auth.AuthHandler;
import org.yomirein.sochatserver.chats.ChatHandler;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.common.managers.ChallengeManager;
import org.yomirein.sochatserver.friendship.FriendsHandler;
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
    public void run() {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

        ChallengeManager challengeManager = new ChallengeManager();
        SessionManager sessionManager = new SessionManager();

        UserRepository userRepository = new UserRepository();
        TrustKeysRepository trustKeysRepository = new TrustKeysRepository();
        ChatRepository chatRepository = new ChatRepository();
        FriendshipRepository friendshipRepository = new FriendshipRepository();
        MessageRepository messageRepository = new MessageRepository();

        AuthService authService = new AuthService(challengeManager, userRepository);
        FriendshipService friendshipService = new FriendshipService(friendshipRepository, userRepository, trustKeysRepository);
        UserService userService = new UserService(friendshipRepository, userRepository, trustKeysRepository);
        ChatService chatService = new ChatService(userService, chatRepository);
        MessageService messageService = new MessageService(messageRepository);

        AuthHandler authHandler = new AuthHandler(userRepository,sessionManager);
        FriendsHandler friendsHandler = new FriendsHandler(sessionManager, userRepository, friendshipRepository, friendshipService, userService);
        UsersHandler usersHandler = new UsersHandler(sessionManager, userRepository, trustKeysRepository, userService);
        ChatHandler chatHandler = new ChatHandler(chatService, userService, messageService, sessionManager);
        MessageHandler messageHandler = new MessageHandler(messageService, chatService, userService, messageRepository, sessionManager);

        HttpServer httpServer = new HttpServer(authService, sessionManager, authHandler, friendsHandler,
                usersHandler, chatHandler, messageHandler, 8081);

        new Thread(() -> {
            try {
                httpServer.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}