package org.yomirein.sochatserver.chats;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.friendship.Friendship;
import org.yomirein.sochatserver.sessions.Session;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserRepository;
import org.yomirein.sochatserver.users.UserService;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.*;
import java.util.stream.Collectors;

import static org.yomirein.sochatserver.utils.MessageSender.notifyUser;
import static org.yomirein.sochatserver.utils.MessageSender.sendError;

@RequiredArgsConstructor
public class ChatHandler {

    private final ChatService chatService;
    private final UserService userService;

    private final SessionManager sessionManager;

    public void getChat(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            ChatDTO chat;
            if (messagePacket.getPayload().hasNonNull("participant_username")) {
                String username = messagePacket.getPayload().get("participant_username").asText();
                User toUser = userService.getUser(username);
                User fromUser = userService.getUser(userId);
                chat = chatService.getChatByUsers(fromUser.getId(), toUser.getId()).toDTO(userId);
            } else if (messagePacket.getPayload().hasNonNull("id")) {
                long id = messagePacket.getPayload().get("id").asLong();
                chat = chatService.getChat(id).toDTO(userId);
            } else {
                throw new IllegalArgumentException("Neither participant username nor chat id present in payload");
            }

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Chat got successfully")
                    .put("chat", JsonConfig.MAPPER.writeValueAsString(chat))
                    .build();

            channelHandlerContext.channel().writeAndFlush(answerPacket);

        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void getUserChats(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId){
        try {
            List<Chat> chats = chatService.getUserChats(userId);
            List<ChatDTO> chatDTOs = chats.stream()
                    .map(chat -> chat.toDTO(userId))
                    .collect(Collectors.toList());
            ArrayNode chatsNode = JsonConfig.MAPPER.createArrayNode();
            for (ChatDTO c : chatDTOs) {
                chatsNode.addPOJO(c);
            }

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Got chats lists")
                    .putNode("chats", chatsNode)
                    .build();

            channelHandlerContext.channel().writeAndFlush(answerPacket);
        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public void createChat(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            User toUser = userService.getUser(messagePacket.getPayload().get("username").asText());
            User fromUser = userService.getUser(userId);

            String toEncryptedKey = messagePacket.getPayload().get("toEncryptedKey").asText();
            String fromEncryptedKey = messagePacket.getPayload().get("fromEncryptedKey").asText();

            Chat chat = chatService.createChat(userId, toUser.getId(), fromEncryptedKey, toEncryptedKey);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Chat created successfully")
                    .put("chat", JsonConfig.MAPPER.writeValueAsString(chat))
                    .build();

            MessagePacket requestInformation = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("server_message", "Chat with user created")
                    .put("chat", JsonConfig.MAPPER.writeValueAsString(chat))
                    .build();

            notifyUser(toUser, requestInformation, sessionManager);
            notifyUser(fromUser, answerPacket, sessionManager);

        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteChat(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId){
        try {
            User toUser = userService.getUser(messagePacket.getPayload().get("username").asText());
            User fromUser = userService.getUser(userId);

            Chat chat = chatService.getChatByUsers(toUser.getId(), fromUser.getId());

            boolean result = chatService.deleteChat(chat);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Chat deleted successfully")
                    .put("chat", JsonConfig.MAPPER.writeValueAsString(chat))
                    .build();

            MessagePacket requestInformation = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("server_message", "Chat deleted")
                    .put("chat", JsonConfig.MAPPER.writeValueAsString(chat))
                    .build();

            notifyUser(fromUser, answerPacket, sessionManager);
            notifyUser(toUser, requestInformation, sessionManager);

        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
