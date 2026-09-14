package org.yomirein.sochatserver.test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.KeyGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.auth.AuthService;
import org.yomirein.sochatserver.calls.CallService;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.chats.ChatType;
import org.yomirein.sochatserver.friendship.Friendship;
import org.yomirein.sochatserver.friendship.FriendshipService;
import org.yomirein.sochatserver.media.MediaService;
import org.yomirein.sochatserver.messages.Message;
import org.yomirein.sochatserver.messages.MessageService;
import org.yomirein.sochatserver.persistance.api.repositories.ChatRepository;
import org.yomirein.sochatserver.persistance.api.repositories.MessageRepository;
import org.yomirein.sochatserver.search.SearchService;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.utils.KeyParser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

    private final FriendshipService friendshipService;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public User createTestUser() {
        try {
            LOGGER.info("Creating test user");
            int randomNum = ThreadLocalRandom.current().nextInt(100);

            // Simply creating user with keys nothing special
            KeyPairGenerator edKpg = KeyPairGenerator.getInstance("Ed25519");
            KeyPair edKp = edKpg.generateKeyPair();
            PublicKey edPublicKey = edKp.getPublic();
            KeyPairGenerator xKpg = KeyPairGenerator.getInstance("X25519");
            KeyPair xKp = xKpg.generateKeyPair();
            PublicKey xPublicKey = xKp.getPublic();
            return userService.createUser(new User("testUser" + randomNum,"test" + randomNum,"desc", edPublicKey, xPublicKey));
        } catch (Exception e) {
            LOGGER.error("Error while test user creating occured: ", e);
        }
        // tf
        return null;
    }

    public Friendship createFriendshipTest(User user1, User user2) {
        try {
            LOGGER.info("Creating test friendship");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest("fingerprintString".getBytes(StandardCharsets.UTF_8));
            String sha256Hex = HexFormat.of().formatHex(hashBytes);

            Friendship request = friendshipService.sendRequest(user1.getId(), user2.getId(), sha256Hex);
            return friendshipService.acceptRequest(request.getId(), sha256Hex);
        } catch (Exception e) {
            LOGGER.error("Error while test friendship creating occured: ", e);
        }
        return null;
    }

    public Chat createDirectChatTest(User user1, User user2) {
        try {
            LOGGER.info("Creating test direct chat");
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, new SecureRandom());
            return chatService.createChat(null, ChatType.PRIVATE,
                Map.of(
                    user2.getId(), KeyParser.convertPublicKeyToString(user2.getX25519PublicKey())
                ),
                user1.getId(), KeyParser.convertPublicKeyToString(user1.getX25519PublicKey()));
        } catch (Exception e) {
            LOGGER.error("Error while test chat creating occured: ", e);
        }
        return null;

    }

    /*
        Logging looks like garbage
        Replace getting single chats with just getting list of last 10 records from tables in future
        No need to test TrustKeys because they are deleting by FK
    */
    public void userDeletionTest() {
        User user1 = createTestUser();
        User user2 = createTestUser();
        Friendship friendship = createFriendshipTest(user1, user2);
        Chat chat = createDirectChatTest(user2, user1);

        Message messageFromUser1 = messageService.addMessage(user1.getId(), chat.getId(), "no ecryption here, my apologies", null, 1, List.<String>of());
        Message messageFromUser2 = messageService.addMessage(user1.getId(), chat.getId(), "no ecryption here, my apologies #2", null, 1, List.<String>of());

        LOGGER.info("Friendship state: " + friendshipService.getByUserAndFriend(user1.getId(), user2.getId()));
        LOGGER.info("Chat state: " + chatService.getUserChats(user2.getId()));
        LOGGER.info("Chat messages: " + messageService.getChatMessages(chat.getId()));

        LOGGER.info("USER 1 DELETED");
        userService.deleteUser(user1.getId());
        try {
            LOGGER.info("Friendship state: " + friendshipService.getByUserAndFriend(user1.getId(), user2.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
        try {
            LOGGER.info("Chat state: " + chatService.getUserChats(user2.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
        try {
            LOGGER.info("Chat messages: " + messageService.getChatMessages(chat.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }

        LOGGER.info("USER 2 DELETED");
        userService.deleteUser(user2.getId());
        try {
            LOGGER.info("Friendship state: " + friendshipService.getByUserAndFriend(user1.getId(), user2.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
        try {
            LOGGER.info("Chat state: " + chatService.getChat(chat.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
        try {
            LOGGER.info("Chat messages: " + messageService.getChatMessages(chat.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
        LOGGER.info("SIMULATING BACKGROUNDCLEANERWORKER");
        messageRepository.deleteOrphaned();
        chatRepository.deleteOrphaned();
        try {
            LOGGER.info("Chat state: " + chatService.getChat(chat.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
        try {
            LOGGER.info("Chat messages: " + messageService.getChatMessages(chat.getId()));
        } catch (Exception e) {
            LOGGER.info("Failed getting");
        }
    }
}
