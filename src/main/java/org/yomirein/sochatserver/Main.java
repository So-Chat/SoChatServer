package org.yomirein.sochatserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.common.managers.ChallengeManager;
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

public class Main {
    public static void main(String[] args) throws Exception {

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

        HttpServer httpServer = new HttpServer(authService, friendshipService, userService, chatService, messageService,
                challengeManager, sessionManager,
                trustKeysRepository, userRepository, friendshipRepository, messageRepository, 8081);

        new Thread(() -> {
            try {
                httpServer.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}